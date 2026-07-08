# Alif Blood Bank - Keystore & Production Release Information

This document contains key information and instructions for the Keystore file used to sign your app for Google Play Store release.

---

## 🔑 Keystore Credentials

We have generated and signed your Release APK and Release AAB using the following keystore file included in your project root:

| Parameter | Value | Description |
|---|---|---|
| **Keystore File** | `my-upload-key.jks` | The physical keystore file. Do NOT lose this file. |
| **Keystore Password** | `alifbloodbankpwd` | Password to access the keystore container. |
| **Key Alias** | `upload` | Alias identifying your signing key. |
| **Key Password** | `alifbloodbankpwd` | Password to access your specific private key. |

---

## 📦 Output Files

Both files are located in the root of your exported project ZIP:
- **Release APK**: `AlifBloodBank_release.apk` (Size: ~12.13 MB) — *For manual installations, testing on Android devices, and instant sharing.*
- **Release AAB**: `AlifBloodBank_release.aab` (Size: ~11.85 MB) — *This is the official Android App Bundle file to upload to Google Play Console.*

---

## 🚀 How to Upload to Google Play Console

1. **Log in to Google Play Console** at [play.google.com/console](https://play.google.com/console).
2. Select your application (**Alif Blood Bank**) or click **Create app**.
3. Go to **Setup** > **App integrity** and follow Google's prompts to let Google manage your App Signing Key (recommended).
4. Create a **Release** (e.g., under Production, Open Testing, or Internal Testing).
5. Drag and drop the **`AlifBloodBank_release.aab`** file (Android App Bundle) into the release editor.
6. Fill in releases notes and click **Review and release**!

---

## 🔒 Security Best Practices

> **IMPORTANT**: Make sure to keep the `my-upload-key.jks` file and these credentials in a secure place. If you lose this keystore file, you will not be able to update your application on the Google Play Store in the future.
