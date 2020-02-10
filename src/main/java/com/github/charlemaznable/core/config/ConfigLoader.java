package com.github.charlemaznable.core.config;

import java.net.URL;
import java.util.List;

public interface ConfigLoader {

    List<URL> loadResources(String basePath);

    Configable loadConfigable(URL url);
}
