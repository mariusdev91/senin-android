# Senin Flutter

`Senin` este noua bază Flutter pentru aplicația de vreme, cu un singur codebase pentru:

- Android
- iOS
- Windows
- Web

## Ce avem acum

- forecast live prin Open-Meteo
- căutare de orașe
- favorite locale
- ecran principal pregătit pentru Android, Windows și iOS
- target iOS configurat cu numele aplicației `Senin`
- bundle id iOS: `com.mariusdev91.senin`

## Rulare locală

```powershell
flutter analyze
flutter test
flutter run -d windows
flutter run -d emulator-5554
```

## iOS

Proiectul iOS există deja în [ios](./ios), dar build-ul iOS final se face pe macOS, cu Xcode.

Pe un Mac, pașii de bază sunt:

1. Instalezi Xcode și CocoaPods.
2. Rulezi `flutter doctor`.
3. Deschizi [Runner.xcworkspace](./ios/Runner.xcworkspace) în Xcode.
4. Alegi un `Team` pentru signing la target-ul `Runner`.
5. Rulezi:

```bash
flutter build ios
```

sau, pentru arhivare App Store / TestFlight:

```bash
flutter build ipa
```

## Surse oficiale utile

- Flutter iOS setup: https://docs.flutter.dev/platform-integration/ios/setup
- Flutter deploy to iOS: https://docs.flutter.dev/deployment/ios
- Flutter build ios / ipa: https://docs.flutter.dev/deployment/ios#build-an-ios-app
