package com.tnh.baseware.core.exceptions;

public class BWCOrgSelectionRequiredException extends BasewareCoreException {

  public BWCOrgSelectionRequiredException(String message) {
    super(message, 400);
  }

  public BWCOrgSelectionRequiredException(String message, Throwable cause) {
    super(message, cause, 400);
  }
}
