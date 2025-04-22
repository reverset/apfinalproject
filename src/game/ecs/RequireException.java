package game.ecs;

import game.RecoverableException;

public class RequireException extends RecoverableException {

	public RequireException(String string) {
		super(string);
	}

}
