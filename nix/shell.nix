{
  pkgs,
  inputs,
  system,
  formatter ? pkgs.alejandra,
  ...
}:
let
  isLinux = pkgs.stdenv.isLinux;
  pinnedJDK = pkgs.jdk21_headless;

  androidCustomPackage = inputs.android-nixpkgs.sdk.${system} (
    sdkPkgs: with sdkPkgs; [
      cmdline-tools-latest
      platform-tools
      build-tools-36-0-0
      platforms-android-36
      emulator
      system-images-android-36-google-apis-playstore-x86-64
    ]
  );

  androidEmulator = pkgs.androidenv.emulateApp {
    name = "emulator";
    platformVersion = "36";
    abiVersion = "x86_64";
    systemImageType = "google_apis_playstore";
    configOptions = {
      "hw.gpu.enabled" = "yes";
      "hw.gpu.mode" = "swiftshader_indirect";
      "hw.keyboard" = "yes";
    };
  };

  androidEmulatorNoGPU = pkgs.androidenv.emulateApp {
    name = "emulator";
    platformVersion = "36";
    abiVersion = "x86_64";
    systemImageType = "google_apis_playstore";
    configOptions = {
      "hw.gpu.enabled" = "no";
      "hw.keyboard" = "yes";
    };
  };
in
pkgs.mkShell {
  packages = [
    pinnedJDK
    androidCustomPackage
    pkgs.ktlint
    pkgs.git
    pkgs.coreutils
    formatter
  ]
  ++ pkgs.lib.optionals isLinux [
    (pkgs.writeScriptBin "android-emulator" ''
      ${androidEmulator}/bin/run-test-emulator
    '')
    (pkgs.writeScriptBin "android-emulator-no-gpu" ''
      ${androidEmulatorNoGPU}/bin/run-test-emulator
    '')
  ];

  env = {
    ANDROID_HOME = "${androidCustomPackage}/share/android-sdk";
    ANDROID_SDK_ROOT = "${androidCustomPackage}/share/android-sdk";
    JAVA_HOME = pinnedJDK.home;
    GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidCustomPackage}/share/android-sdk/build-tools/36.0.0/aapt2";
  };

  shellHook = ''
    echo "---------------------------------------------------------------------------------------------------"
    echo " Uchar Element X Android dev shell."
    echo " JDK:           $(java -version 2>&1 | head -1)"
    echo " ANDROID_HOME:  $ANDROID_HOME"
    ${pkgs.lib.optionalString isLinux ''
      echo " Emulator:      run 'android-emulator' else 'android-emulator-no-gpu'"
    ''}
    echo "---------------------------------------------------------------------------------------------------"
  '';
}
