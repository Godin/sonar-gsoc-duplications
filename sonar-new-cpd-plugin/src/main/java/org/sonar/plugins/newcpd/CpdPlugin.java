package org.sonar.plugins.newcpd;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.SonarPlugin;

public class CpdPlugin extends SonarPlugin {

  public List getExtensions() {
    return Arrays.asList(CpdSensor.class);
  }

}
