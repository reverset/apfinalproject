use std::{fs::File, io::Read};

const COMPTIME_GAME_JAR_PATH: &str = "../game.jar";

fn main() {
    let mut file = File::open(COMPTIME_GAME_JAR_PATH).expect("comptime game.jar is missing.");
    let mut buffer = Vec::new();
    let _ = file.read_to_end(&mut buffer);    

    let hash = seahash::hash(&buffer);
    println!("cargo:rustc-env=GAME_HASH={}", hash);
}