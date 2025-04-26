#![windows_subsystem = "windows"]

use std::{fs::File, io::Read, process::Command};

const GAME_JAR_HASH: u64 = 10436466817374203798;
const JAVAW_HASH: u64 = 11076107172332908793;

fn check_game_jar() -> bool {
    let mut file = File::open("game.jar").expect("Game.jar is missing.");
    let mut buffer = Vec::new();
    let _ = file.read_to_end(&mut buffer);    

    let result = seahash::hash(&buffer);
    GAME_JAR_HASH == result
}

fn check_javaw() -> bool {
    let mut file = File::open(".\\bin\\javaw.exe").expect(".\\bin\\javaw is missing.");
    let mut buffer = Vec::new();
    let _ = file.read_to_end(&mut buffer);    

    let result = seahash::hash(&buffer);
    JAVAW_HASH == result
}

fn main() {
    if check_game_jar() && check_javaw() {
        Command::new(".\\bin\\javaw.exe")
            .args(["-jar", "game.jar"])
            .spawn().expect("failed to launch game.");
    }

}
