# 簡訊自動轉寄

Android 原生 app，適用雙卡手機。指定接收 SIM、發送 SIM 與目標手機門號後，自動轉寄啟用期間收到的新 SMS。第一版不包含 Gmail、MMS、RCS、歷史訊息掃描或內容篩選。

## 操作

1. 開啟 app，授予電話狀態、接收簡訊與發送簡訊權限。
2. 選擇要偵測及發送的 SIM，填入可接收簡訊的目標門號。
3. 儲存設定；未儲存的修改不影響自動轉寄。
4. 使用「發送測試簡訊」驗證目前儲存的發送設定，並在目標手機確認實收。
5. 啟用自動轉寄並儲存，使用外部手機向選定 SIM 發送測試來訊。

真實發送依電信資費計費，長簡訊可能拆成多段。請勿把目標設定為本機門號，亦勿讓多支轉寄手機互相轉寄。系統回報送出成功不代表收件方已讀；送達回條取決於電信支援。

## 語言

支援英文、繁體中文與依系統設定。從 App 語言選單切換，Android 13 以上亦可使用系統的個別 App 語言設定。切換語言會保留尚未儲存的轉寄設定；固定測試簡訊與轉寄標頭使用同一 App 語言，原始簡訊本文保持不變。

紀錄以穩定狀態代碼保存，依目前語言顯示。舊版已知紀錄會自動轉換；無法辨識的舊結果顯示為未確認，不將其誤判為成功。轉寄使用固定 `[SMSForwarder]` 標記，也辨識舊版標記以降低循環風險。

## 背景與重開機

透過系統 SMS 接收廣播觸發，不靠常駐輪詢。一般離開畫面與鎖屏後設定仍有效；重開機後首次解鎖可恢復運作。首次安裝必須開啟並授權。系統「強制停止」、撤銷權限、無訊號、SIM失效或廠商限制會阻止接收或發送；強制停止後須再次開啟 app。部分手機的省電或背景管理設定可能限制簡訊廣播；請在應用程式設定中檢查電池及背景執行限制，並以實際鎖屏收訊確認。

## 隱私與安全

不讀取既有簡訊，不使用網路或雲端服務。app 的紀錄不保存簡訊內容；Android 可能將已發送訊息寫入系統簡訊 app。依指定 subscription 發送，不在SIM失效時改用預設SIM。轉寄失敗與結果不明不自動重送，避免重複與額外費用。每則最多 10 段，包含測試在內每小時最多 30 則；超限留下狀態且不自動補發。程式具本機去重與循環標記保護；本機門號可能無法讀取，因此使用者必須確認目標不是本機 SIM，亦無法保證辨識所有跨裝置轉寄迴圈。

## 建置

需要 JDK 17、Android SDK 36 與可取得 Gradle 相依套件的環境。

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

開發用 APK：`app/build/outputs/apk/debug/app-debug.apk`。ADB 安裝後，從手機啟動 app 設定。目前尚待 F-Droid 官方收錄；未進行 Google Play 發布審核。1.0.1 採開發者簽章 RB，流程與待驗收事項見[審閱清單](fdroid/RB_REVIEW.md)。

## 授權與支援

本專案採 [MIT 授權](LICENSE)。問題回報、功能建議與維護討論一律使用 [GitHub Issues](https://github.com/twkevinzhang/sms-forwarder/issues)；程式貢獻透過 GitHub Pull Requests。請勿在公開回報附上真實門號、簡訊本文、驗證碼、PIN 或私鑰。

## 發布

正式 application ID 為 `com.twkevinzhang.smsforwarder`。[v1.0.0 原始碼版本](https://github.com/twkevinzhang/sms-forwarder/releases/tag/v1.0.0)已發布，該版文案、圖示與截圖已完成[素材審閱](STORE_LISTING_DRAFT.md)。發布流程見[發布說明](fdroid/README.md)，歷史建置證據見[驗證紀錄](fdroid/BUILD_VERIFICATION.md)。2026-10-15 已提交 [F-Droid 收錄申請 !52669](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/52669)，1.0.0 的 fork 與 MR 流水線均已通過。審查者要求 RB 與 R8；對應的 1.0.1 變更已獲授權採開發者簽章 RB，正式發布與驗證結果記錄於發布說明。
