package com.example.petstore.mgmt.message;

/**
 * mgmt-service（機能別コード: 001）のメッセージコード定義。
 *
 * <p>形式: {メッセージ種別}{機能別コード}{コード枝番}（計7桁）
 */
public enum MgmtMessageCode {
  PET_NOT_FOUND("E001001"),
  PET_DUPLICATE("E001002");

  private final String code;

  MgmtMessageCode(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
