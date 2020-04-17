package com.nandur.qrcodescanner.history;

public class QrModel {
  private int id;
  private String path, content, date;
  private int viewType;

  public QrModel(int id, String path, String content, String date, int viewType) {
    this.id = id;
    this.path = path;
    this.content = content;
    this.date = date;
    this.viewType = viewType;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public int getViewType() {
    return viewType;
  }

  public void setViewType(int viewType) {
    this.viewType = viewType;
  }
}