#include <windows.h>

int main(int argc, char** argv) {
    ShellExecute(NULL, "open", ".\\bin\\javaw.exe", "-XX:+UseZGC -XX:+ZGenerational -Xmx1g -Xms1g -XX:+AlwaysPreTouch -XX:-ZUncommit -jar game.jar", NULL, SW_SHOWNORMAL);

    return 0;
}