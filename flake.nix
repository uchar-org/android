{
  description = "uchar element x — reproducible dev environment";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-25.11";

    flake-parts.url = "github:hercules-ci/flake-parts";

    android-nixpkgs = {
      url = "github:tadfisher/android-nixpkgs";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs =
    { self, flake-parts, ... }@inputs:
    flake-parts.lib.mkFlake { inherit inputs; } (
      { ... }:
      {
        systems = [
          "x86_64-linux"
          "aarch64-linux"
          "x86_64-darwin"
          "aarch64-darwin"
        ];

        perSystem =
          { pkgs, system, ... }:
          {
            _module.args.pkgs = import inputs.nixpkgs {
              inherit system;
              config.allowUnfree = true;
              config.android_sdk.accept_license = true;
              overlays = [
                (final: prev: {
                  android = inputs.android-nixpkgs;
                })
              ];
            };

            formatter = pkgs.alejandra;

            devShells.default = import ./nix/shell.nix {
              inherit pkgs inputs system;
              formatter = pkgs.alejandra;
            };
          };
      }
    );
}
