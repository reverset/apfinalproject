import subprocess

subprocess.run(
    "jar cvfm game.jar ../MANIFEST.MF -C . .",
    shell=True,
    check=True,
    cwd="./test/"
)