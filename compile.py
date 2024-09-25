import subprocess

subprocess.run(
    "javac --class-path ./jaylib.jar ./src/game/*.java ./src/game/ecs/*.java ./src/game/ecs/comps/*.java ./src/game/core/*.java ./src/game/core/rendering/*.java -d ./test/",
    shell=True,
    check=True,
)

subprocess.run(
    "java -XX:+UseZGC -XX:+ZGenerational -verbose:gc game.Game",
    shell=True,
    check=True,
    cwd="./test/"
)
