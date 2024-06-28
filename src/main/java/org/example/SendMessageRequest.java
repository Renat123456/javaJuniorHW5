package org.example;

/**
 * {
 *   "type": "sendMessage",
 *   "recipient: "nagibator",
 *   "message": "text to nagibator"
 * }
 */
public class SendMessageRequest {
  private String type;
  private String recipient;
  private String message;

  public SendMessageRequest() {

  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
