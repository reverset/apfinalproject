#![windows_subsystem = "windows"]

use std::{fs::File, io::Read, process::Command};

const GAME_JAR_HASH: &str = env!("GAME_HASH");

const JAVAW_HASH: u64 = 11076107172332908793;

const GAME_JAR_PATH: &str = "game.jar";

#[cfg(target_os = "windows")]
const JAVAW_PATH: &str = ".\\bin\\javaw.exe";

#[cfg(target_os = "linux")]
const JAVAW_PATH: &str = "./bin/javaw";

fn check_game_jar() -> bool {
    let mut file = File::open(GAME_JAR_PATH).expect("game.jar is missing.");
    let mut buffer = Vec::new();
    let _ = file.read_to_end(&mut buffer);    

    let result = seahash::hash(&buffer);
    GAME_JAR_HASH == result.to_string()
}

fn check_javaw() -> bool {
    let mut file = File::open(JAVAW_PATH).expect("./bin/javaw is missing.");
    let mut buffer = Vec::new();
    let _ = file.read_to_end(&mut buffer);    

    let result = seahash::hash(&buffer);
    JAVAW_HASH == result
}

fn main() {
    if check_game_jar() && check_javaw() {
        Command::new(JAVAW_PATH)
            .args(["-XX:+UseZGC", "-XX:+ZGenerational", "-Xmx1g", "-Xms1g", "-XX:+AlwaysPreTouch", "-XX:-ZUncommit", "-jar", GAME_JAR_PATH])
            .spawn().expect("failed to launch game.");
    }

}
