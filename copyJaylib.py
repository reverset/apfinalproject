import subprocess

subprocess.run(
    "jar xvf ../jaylib.jar",
    shell=True,
    check=True,
    cwd="./test/"
)