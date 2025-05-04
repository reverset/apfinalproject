package game.core;

public enum GameTags {
    PLAYER,
    PLAYER_TEAM,

    ENEMY_TEAM,
    BULLET;

    public static final Object[] ENEMY_TEAM_TAGS = new Object[]{GameTags.ENEMY_TEAM};
    public static final Object[] PLAYER_TEAM_TAGS = new Object[]{GameTags.PLAYER_TEAM};
    public static final Object[] NONE = new Object[]{};
}
