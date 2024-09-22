javac --class-path .\jaylib.jar .\src\game\*.java .\src\game\ecs\*.java .\src\game\ecs\comps\*.java .\src\game\core\*.java .\src\game\core\rendering\*.java -d .\test\

cd .\test\
@rem jar xvf ..\jaylib.jar
java -XX:+UseZGC -XX:+ZGenerational game.Game
cd ..\

