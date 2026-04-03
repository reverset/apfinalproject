import subprocess
import platform

subprocess.run(
    "javac -Xlint:deprecation --class-path ./jaylib.jar ./src/game/*.java ./src/game/ecs/*.java ./src/game/ecs/comps/*.java ./src/game/core/*.java ./src/game/core/rendering/*.java -d ./test/",
    shell=True,
    check=True,
)

macAdditional = ""
if platform.system() == "Darwin":
    macAdditional = "-XstartOnFirstThread"

subprocess.run(
    f"java --enable-native-access=ALL-UNNAMED {macAdditional} -XX:+UseZGC -Xmx1g -Xms1g -XX:+AlwaysPreTouch -XX:-ZUncommit -verbose:gc game.Game",
    shell=True,
    check=True,
    cwd="./test/"
)
