package com.github.charlemaznable.lang;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.github.charlemaznable.lang.Str.isEmpty;
import static com.github.charlemaznable.lang.Str.toStr;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.google.common.io.Resources.readLines;
import static java.lang.Class.forName;
import static java.lang.ClassLoader.getSystemResources;
import static java.lang.System.identityHashCode;
import static java.lang.Thread.currentThread;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.joor.Reflect.on;

public class ClzPath {

    public static boolean classExists(String className) {
        try {
            forName(className, false, getClassLoader());
            return true;
        } catch (Throwable e) { // including ClassNotFoundException
            return false;
        }
    }

    public static Class<?> findClass(String className) {
        if (isEmpty(className)) return null;

        try {
            return forName(className, false, getClassLoader());
        } catch (ClassNotFoundException ignore) {
        }
        return null;
    }

    /*
     * Load a class given its name. BL: We wan't to use a known ClassLoader--hopefully the heirarchy is set correctly.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> tryLoadClass(String className) {
        if (isEmpty(className)) return null;

        try {
            return (Class<T>) getClassLoader().loadClass(className);
        } catch (ClassNotFoundException ignore) {
        }
        return null;
    }

    /**
     * Return the context classloader. BL: if this is command line operation, the classloading issues are more sane.
     * During servlet execution, we explicitly set the ClassLoader.
     *
     * @return The context classloader.
     */
    public static ClassLoader getClassLoader() {
        return currentThread().getContextClassLoader();
    }

    public static URL classResource(String classPath) {
        return getClassLoader().getResource(classPath);
    }

    public static boolean classResourceExists(String classPath) {
        return classResource(classPath) != null;
    }

    public static InputStream classResourceAsInputStream(String classPath) {
        return getClassLoader().getResourceAsStream(classPath);
    }

    public static String classResourceAsString(String classPath) {
        return urlAsString(classResource(classPath));
    }

    public static List<String> classResourceAsLines(String classPath) {
        return urlAsLines(classResource(classPath));
    }

    public static InputStream urlAsInputStream(URL url) {
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    public static String urlAsString(URL url) {
        try {
            return url != null ? Resources.toString(url, Charsets.UTF_8) : null;
        } catch (IOException e) {
            return null;
        }
    }

    public static List<String> urlAsLines(URL url) {
        try {
            return url != null ? readLines(url, Charsets.UTF_8) : null;
        } catch (IOException e) {
            return null;
        }
    }

    @SneakyThrows
    public static URL[] classResources(String basePath, String extension) {
        return ExtensionMatchClzResources.classResources(basePath, extension);
    }

    private static class ExtensionMatchClzResources {

        public static final String URL_PROTOCOL_VFSFILE = "vfsfile";
        private static final String FILE_URL_PREFIX = "file:";
        private static final String JAR_URL_PREFIX = "jar:";
        private static final String URL_PROTOCOL_FILE = "file";
        private static final String URL_PROTOCOL_JAR = "jar";
        private static final String URL_PROTOCOL_ZIP = "zip";
        private static final String URL_PROTOCOL_WSJAR = "wsjar";
        private static final String URL_PROTOCOL_VFSZIP = "vfszip";
        private static final String URL_PROTOCOL_VFS = "vfs";

        private static final String JAR_FILE_EXTENSION = ".jar";

        private static final String JAR_URL_SEPARATOR = "!/";

        private static boolean equinoxResolveMethodExists
                = classExists("org.eclipse.core.runtime.FileLocator");

        public static URL[] classResources(String basePath, String extension) throws IOException {
            String base = resolveBasePath(basePath);
            String extn = resolveExtension(extension);
            URL[] rootDirResources = findRootClassResources(base);
            Set<URL> result = new LinkedHashSet<>(16);
            for (URL rootDirResource : rootDirResources) {
                rootDirResource = resolveRootDirResource(rootDirResource);
                if (rootDirResource.getProtocol().startsWith(URL_PROTOCOL_VFS)) {
                    result.addAll(VfsResourceMatchingDelegate.findMatchingResources(rootDirResource, extension));
                } else if (isJarResource(rootDirResource)) {
                    result.addAll(findExtMatchingJarResources(rootDirResource, extn));
                } else {
                    result.addAll(findExtMatchingFileResources(rootDirResource, extn));
                }
            }
            return result.toArray(new URL[result.size()]);
        }

        public static boolean isReadable(URL url) {
            try {
                String protocol = url.getProtocol();
                if (URL_PROTOCOL_FILE.equals(protocol) || URL_PROTOCOL_VFSFILE.equals(protocol) ||
                        URL_PROTOCOL_VFS.equals(protocol)) {
                    // Proceed with file system resolution...
                    File file = getFile(url);
                    return (file.canRead() && !file.isDirectory());
                } else {
                    return true;
                }
            } catch (IOException ex) {
                return false;
            }
        }

        private static String resolveBasePath(String basePath) {
            if (isEmpty(basePath)) return toStr(basePath);
            if (basePath.startsWith("/")) basePath = basePath.substring(1);
            if (!basePath.endsWith("/")) basePath = basePath + "/";
            return basePath;
        }

        private static String resolveExtension(String extension) {
            if (isEmpty(extension)) return toStr(extension);
            if (!extension.startsWith(".")) extension = "." + extension;
            return extension;
        }

        private static URL[] findRootClassResources(String basePath) throws IOException {
            Set<URL> result = new LinkedHashSet<>(16);
            ClassLoader classLoader = getClassLoader();
            Enumeration<URL> urlEnumer = (classLoader != null ?
                    classLoader.getResources(basePath) : getSystemResources(basePath));
            while (urlEnumer.hasMoreElements()) {
                result.add(urlEnumer.nextElement());
            }
            if ("".equals(basePath)) {
                // The above result is likely to be incomplete, i.e. only containing file system references.
                // We need to have pointers to each of the jar files on the classpath as well...
                addAllClassLoaderJarRoots(classLoader, result);
            }
            return result.toArray(new URL[result.size()]);
        }

        private static void addAllClassLoaderJarRoots(ClassLoader classLoader, Set<URL> result) {
            if (classLoader instanceof URLClassLoader) {
                try {
                    URL[] urls = ((URLClassLoader) classLoader).getURLs();
                    for (URL url : urls) {
                        if (isJarFileURL(url)) {
                            URL newURL = new URL(JAR_URL_PREFIX +
                                    url.toString() + JAR_URL_SEPARATOR);
                            if (existsJarFileURL(newURL)) result.add(newURL);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            if (classLoader != null) {
                try {
                    addAllClassLoaderJarRoots(classLoader.getParent(), result);
                } catch (Exception ignored) {
                }
            }
        }

        private static boolean isJarFileURL(URL url) {
            return (URL_PROTOCOL_FILE.equals(url.getProtocol()) &&
                    url.getPath().toLowerCase().endsWith(JAR_FILE_EXTENSION));
        }

        private static boolean existsJarFileURL(URL url) {
            try {
                // Try a URL connection content-length header...
                URLConnection con = url.openConnection();
                customizeConnection(con);
                HttpURLConnection httpCon = con instanceof HttpURLConnection ?
                        (HttpURLConnection) con : null;
                if (httpCon != null) {
                    int code = httpCon.getResponseCode();
                    if (code == HTTP_OK) {
                        return true;
                    } else if (code == HTTP_NOT_FOUND) {
                        return false;
                    }
                }
                if (con.getContentLength() >= 0) return true;
                if (httpCon != null) {
                    // no HTTP OK status, and no content-length header: give up
                    httpCon.disconnect();
                    return false;
                } else {
                    // Fall back to stream existence: can we open the stream?
                    InputStream is = getInputStream(url);
                    is.close();
                    return true;
                }
            } catch (IOException ex) {
                return false;
            }
        }

        private static void customizeConnection(URLConnection con) throws IOException {
            useCachesIfNecessary(con);
            if (con instanceof HttpURLConnection) customizeConnection((HttpURLConnection) con);
        }

        private static void useCachesIfNecessary(URLConnection con) {
            con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
        }

        private static void customizeConnection(HttpURLConnection con) throws IOException {
            con.setRequestMethod("HEAD");
        }

        private static InputStream getInputStream(URL url) throws IOException {
            URLConnection con = url.openConnection();
            useCachesIfNecessary(con);
            try {
                return con.getInputStream();
            } catch (IOException ex) {
                // Close the HTTP connection (if applicable).
                if (con instanceof HttpURLConnection) {
                    ((HttpURLConnection) con).disconnect();
                }
                throw ex;
            }
        }

        private static URL resolveRootDirResource(URL original) {
            if (equinoxResolveMethodExists &&
                    original.getProtocol().startsWith("bundle"))
                return on("org.eclipse.core.runtime.FileLocator")
                        .call("resolve", original).get();

            return original;
        }

        private static boolean isJarResource(URL url) {
            String protocol = url.getProtocol();
            return URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_ZIP.equals(protocol) ||
                    URL_PROTOCOL_VFSZIP.equals(protocol) || URL_PROTOCOL_WSJAR.equals(protocol);
        }

        private static Set<URL> findExtMatchingJarResources(URL rootDirResource, String extension) throws IOException {
            URLConnection con = rootDirResource.openConnection();
            JarFile jarFile;
            String rootEntryPath;
            boolean newJarFile = false;

            if (con instanceof JarURLConnection) {
                // Should usually be the case for traditional JAR files.
                JarURLConnection jarCon = (JarURLConnection) con;
                useCachesIfNecessary(jarCon);
                jarFile = jarCon.getJarFile();
                JarEntry jarEntry = jarCon.getJarEntry();
                rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
            } else {
                // No JarURLConnection -> need to resort to URL file parsing.
                // We'll assume URLs of the format "jar:path!/entry", with the protocol
                // being arbitrary as long as following the entry format.
                // We'll also handle paths with and without leading "file:" prefix.
                String urlFile = rootDirResource.getFile();
                int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
                if (separatorIndex != -1) {
                    rootEntryPath = urlFile.substring(separatorIndex + JAR_URL_SEPARATOR.length());
                    jarFile = getJarFile(urlFile.substring(0, separatorIndex));
                } else {
                    jarFile = new JarFile(urlFile);
                    rootEntryPath = "";
                }
                newJarFile = true;
            }

            try {
                if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
                    // Root entry path must end with slash to allow for proper matching.
                    // The Sun JRE does not return a slash here, but BEA JRockit does.
                    rootEntryPath = rootEntryPath + "/";
                }
                Set<URL> result = new LinkedHashSet<>(8);
                for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                    JarEntry entry = entries.nextElement();
                    String entryPath = entry.getName();
                    if (entryPath.startsWith(rootEntryPath) &&
                            entryPath.endsWith(extension)) {
                        String relativePath = entryPath.substring(rootEntryPath.length());
                        if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
                        result.add(new URL(rootDirResource, relativePath));
                    }
                }
                return result;
            } finally {
                // Close jar file, but only if freshly obtained -
                // not from JarURLConnection, which might cache the file reference.
                if (newJarFile) jarFile.close();
            }
        }

        private static JarFile getJarFile(String jarFileUrl) throws IOException {
            if (jarFileUrl.startsWith(FILE_URL_PREFIX)) {
                try {
                    return new JarFile(toURI(jarFileUrl).getSchemeSpecificPart());
                } catch (URISyntaxException ex) {
                    // Fallback for URLs that are not valid URIs (should hardly ever happen).
                    return new JarFile(jarFileUrl.substring(FILE_URL_PREFIX.length()));
                }
            } else {
                return new JarFile(jarFileUrl);
            }
        }

        private static URI toURI(String location) throws URISyntaxException {
            return new URI(replace(location, " ", "%20"));
        }

        private static Set<URL> findExtMatchingFileResources(URL rootDirResource, String extension) throws IOException {
            File rootDir;
            try {
                rootDir = getFile(rootDirResource).getAbsoluteFile();
            } catch (IOException ex) {
                return emptySet();
            }
            return doFindMatchingFileSystemResources(rootDir, extension);
        }

        private static File getFile(URL url) throws IOException {
            if (url.getProtocol().startsWith(URL_PROTOCOL_VFS))
                return VfsFileDelegate.getFile(url);

            if (!URL_PROTOCOL_FILE.equals(url.getProtocol()))
                throw new FileNotFoundException(
                        "URL [" + url + "] cannot be resolved to absolute file path " +
                                "because it does not reside in the file system: " + url);

            try {
                return new File(toURI(url.toString()).getSchemeSpecificPart());
            } catch (URISyntaxException ex) {
                // Fallback for URLs that are not valid URIs (should hardly ever happen).
                return new File(url.getFile());
            }
        }

        private static Set<URL> doFindMatchingFileSystemResources(File rootDir, String extension) throws IOException {
            Set<File> matchingFiles = retrieveMatchingFiles(rootDir, extension);
            Set<URL> result = new LinkedHashSet<>(matchingFiles.size());
            for (File file : matchingFiles) {
                result.add(file.toURI().toURL());
            }
            return result;
        }

        private static Set<File> retrieveMatchingFiles(File rootDir, String extension) {
            if (!rootDir.exists() || !rootDir.isDirectory() || !rootDir.canRead()) return emptySet();

            Set<File> result = new LinkedHashSet<>(8);
            doRetrieveMatchingFiles(extension, rootDir, result);
            return result;
        }

        private static void doRetrieveMatchingFiles(String extension, File dir, Set<File> result) {
            File[] dirContents = dir.listFiles();
            if (dirContents == null) return;

            String abnormalPattern = File.separator + extension;
            for (File content : dirContents) {
                String currPath = replace(content.getAbsolutePath(), File.separator, "/");
                if (content.isDirectory() && content.canRead()) {
                    doRetrieveMatchingFiles(extension, content, result);
                } else if (currPath.endsWith(extension) &&
                        !currPath.endsWith(abnormalPattern)) {
                    result.add(content);
                }
            }
        }
    }

    private static class VfsFileDelegate {

        private static final String VFS3_PKG = "org.jboss.vfs.";

        public static File getFile(URL url) {
            return getFile(getRoot(url));
        }

        public static Object getRoot(URL url) {
            return on(VFS3_PKG + "VFS").call("getChild", url).get();
        }

        public static URL getURL(Object vfsResource) {
            return on(vfsResource).call("toURL").get();
        }

        public static File getFile(Object vfsResource) {
            return on(vfsResource).call("getPhysicalFile").get();
        }

        public static String getPath(Object vfsResource) {
            return on(vfsResource).call("getPathName").get();
        }

        public static Object getVisitorAttribute() {
            return on(VFS3_PKG + "VisitorAttributes").field("RECURSE").get();
        }

        public static void visit(Object resource, InvocationHandler visitor) {
            on(resource).call("visit", newProxyInstance(getClassLoader(),
                    new Class<?>[]{getVirtualFileVisitor()}, visitor));
        }

        private static Class<?> getVirtualFileVisitor() {
            return on(VFS3_PKG + "VirtualFileVisitor").get();
        }
    }

    private static class VfsResourceMatchingDelegate {

        public static Set<URL> findMatchingResources(URL rootResource, String extension) {
            Object root = VfsFileDelegate.getRoot(rootResource);
            ExtensionMatchVFVisitor visitor = new ExtensionMatchVFVisitor(
                    VfsFileDelegate.getPath(root), extension);
            VfsFileDelegate.visit(root, visitor);
            return visitor.getResources();
        }
    }

    private static class ExtensionMatchVFVisitor implements InvocationHandler {

        private final String extension;

        private final String rootPath;

        private final Set<URL> resources = newLinkedHashSet();

        public ExtensionMatchVFVisitor(String rootPath, String extension) {
            this.extension = extension;
            this.rootPath = (rootPath.length() == 0 || rootPath.endsWith("/") ? rootPath : rootPath + "/");
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();
            if (Object.class.equals(method.getDeclaringClass())) {
                if (methodName.equals("equals")) {
                    // Only consider equal when proxies are identical.
                    return (proxy == args[0]);
                } else if (methodName.equals("hashCode")) {
                    return identityHashCode(proxy);
                }
            } else if ("getAttributes".equals(methodName)) {
                return getAttributes();
            } else if ("visit".equals(methodName)) {
                visit(args[0]);
                return null;
            } else if ("toString".equals(methodName)) {
                return toString();
            }

            throw new IllegalStateException("Unexpected method invocation: " + method);
        }

        public void visit(Object vfsResource) {
            try {
                if (VfsFileDelegate.getPath(vfsResource)
                        .substring(this.rootPath.length())
                        .endsWith(this.extension))
                    this.resources.add(VfsFileDelegate.getURL(vfsResource));
            } catch (Exception ex) {
                throw new RuntimeException("Failed to obtain URL for file " + vfsResource, ex);
            }
        }

        public Object getAttributes() {
            return VfsFileDelegate.getVisitorAttribute();
        }

        public Set<URL> getResources() {
            return this.resources;
        }

        public int size() {
            return this.resources.size();
        }

        @Override
        public String toString() {
            return "extension: " + this.extension + ", resources: " + this.resources;
        }
    }
}
