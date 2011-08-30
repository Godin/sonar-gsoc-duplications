package org.sonar.duplications.benchmark.db;

public class Snapshot {

  private Integer id;
  private String project;

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

}
