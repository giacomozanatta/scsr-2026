{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};

        jdk = pkgs.jdk17;
        gradle = pkgs.gradle_8;

        gradleWithJdk = pkgs.writeShellScriptBin "gradle" ''
          export JAVA_HOME=${jdk}
          exec ${gradle}/bin/gradle "$@"
        '';
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = [ jdk gradleWithJdk ];

          shellHook = ''
            java -version
            gradle --version
          '';
        };
      });
}
