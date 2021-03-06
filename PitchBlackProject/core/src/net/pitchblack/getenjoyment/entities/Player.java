package net.pitchblack.getenjoyment.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;  // powerful! is a 2d vector
import com.badlogic.gdx.physics.box2d.Body;

import net.pitchblack.getenjoyment.entities.Player.State;
import net.pitchblack.getenjoyment.helpers.PBAssetManager;
import net.pitchblack.getenjoyment.logic.CollisionHandler;
import net.pitchblack.getenjoyment.logic.GameRenderer;
import net.pitchblack.getenjoyment.logic.GameWorld;

public class Player {
	public static final float SPEED = 1.5f;

	public static final Vector2 SPEED_VECTOR = new Vector2(SPEED, 0);
	public static final float JUMP_FORCE = 5.5f;
	public static final float TERMINAL_VELOCITY = 10f;
	private static final int JUMP_LIMIT = 1000; // 2
	
	private Vector2 position;
	private Vector2 velocity;
	private Body body;
	private final int id;
	
	private float height;
	private float width;
	private Rectangle boundRect;
	
	private State state;
	private boolean pushState;
	private int jumps;  // number of jumps, capped at JUMP_LIMIT
	private boolean movementLeft;
	private boolean movementRight;
	
	private TiledMapTileLayer collisionLayer; // in server, will update so often if player moves to different map 
	
	public enum State {
		  ASCENDING,
		  DESCENDING,
		  STANDING,
		  LEFT,
		  RIGHT,
		  DEAD
		}
	
	public Player(int id, Body body, float height, float width) {
		this.id = id;
		this.height = height;
		this.width = width;
		
		position = new Vector2(GameWorld.START_POS_X, GameWorld.START_POS_Y);
		velocity = new Vector2(0, 0);
		this.body = body;
		body.setFixedRotation(true);
		body.setSleepingAllowed(false);
		
		state = State.STANDING;
		pushState = false;
		movementLeft = false;
		movementRight = false;
	}

	public void update(float delta) {
		float oldY = position.y;
		float oldX = position.x;
		
		if(state != State.DEAD) {
			if(body.getLinearVelocity().y < 0 && jumps != 0) {
				state = State.DESCENDING;
			}
			
			//System.out.println(pushState);
			//System.out.print(" " + state);
			//System.out.println(body.getPosition().toString());
			
			if(movementLeft && body.getLinearVelocity().x > -TERMINAL_VELOCITY) {
				// if pushing, divide by 2
				body.applyLinearImpulse(pushState ? SPEED_VECTOR.cpy().scl(1/-50f) : SPEED_VECTOR.cpy().scl(-1), body.getWorldCenter(), true);
			}
			
			if(movementRight && body.getLinearVelocity().x < TERMINAL_VELOCITY) {
				body.applyLinearImpulse(pushState ? SPEED_VECTOR.cpy().scl(1/50f) : SPEED_VECTOR, body.getWorldCenter(), true);
			}
		}
	}

	
	public void keyDown(int keycode) {
		switch(keycode) {
			case Keys.A:
				//System.out.println("A Down");
				movementLeft = true;
				//velocity.x += -SPEED;
				//Vector2 velA = body.getLinearVelocity();
				//velA.x = -SPEED;
				//body.setLinearVelocity(velA);
				//body.setLinearVelocity(body.getLinearVelocity().y - SPEED, body.getLinearVelocity().y);
				break;
			case Keys.D:
				//System.out.println("D Down");
				movementRight= true;
				//velocity.x += SPEED;
				//Vector2 velD = body.getLinearVelocity();
				//velD.x = SPEED;
				//body.setLinearVelocity(velD);
				//body.applyForceToCenter(new Vector2(0, SPEED), true);
				//body.applyLinearImpulse(new Vector2(SPEED, 0), body.getWorldCenter(), true);
				break;
			case Keys.SPACE:
				//System.out.println("Space Down");
// 				if(state == State.STANDING) {  // so cannot keep jumping
//					velocity.y += JUMP_VELOCITY;
//					state = State.ASCENDING;
//				}
				//body.setLinearVelocity(body.getLinearVelocity().x, JUMP_FORCE);
				//body.setLinearVelocity(0, JUMP_FORCE);
//	            
//				break;
//				
				if(jumps < JUMP_LIMIT) {
					Vector2 vel2 = body.getLinearVelocity();
					Vector2 pos = body.getPosition();
					
					//body.applyForce(new Vector2(0, JUMP_FORCE), body.getWorldCenter(), true);
					//body.applyForceToCenter(0, 35f, true);
					body.applyLinearImpulse(new Vector2(0, JUMP_FORCE), body.getWorldCenter(), true);
					state = State.ASCENDING;
					jumps++;
				}
				break;	
		}
	}
	
	public void keyUp(int keycode) {
		switch(keycode) {
			case Keys.A:  // same as d
				//System.out.println("A Up");
				Vector2 vel1 = body.getLinearVelocity();
				vel1.x = 0;
				body.setLinearVelocity(vel1);
				movementLeft = false;
				break;
//				Vector2 velA = body.getLinearVelocity();
//				//velocity.x += SPEED;
//				body.setLinearVelocity(0, body.getLinearVelocity().y);
//				break;
			case Keys.D:
				//System.out.println("D Up");
				//velocity.x += -SPEED;
				Vector2 vel2 = body.getLinearVelocity();
				vel2.x = 0;
				body.setLinearVelocity(vel2);
				movementRight = false;
				break;
		}
	}

	public boolean hasMoved() {
		return !movementRight || !movementLeft;
	}

	public float getX() {
		return body.getPosition().x - (width / 2) + (3 / GameWorld.PPM);
	}
	
	public float getY() {
		return body.getPosition().y - (height / 2) - (3 / GameWorld.PPM);
	}
	
	public Vector2 getVelocity(){
		return body.getLinearVelocity().cpy();
	}

	public void setState(State state) {
		if(state == State.STANDING) {
			jumps = 0;
		}
		this.state = state;
	}
	
	public void setPushState(Boolean flag){
		pushState = flag;
	}

	public void removePushVelocity(float x) {
		Vector2 vel = body.getLinearVelocity();
		vel.x -= x;
		body.setLinearVelocity(vel);
	}

	public Body getBody() {
		return body;
	}
	
	public int getID() {
		return id;
	}

	public void kill() {
		state = State.DEAD;
	}
	
	@Override
	public String toString() {
		return id + "," + getX() + "," + getY() + "," + 
			   state + "," + movementLeft + "," + movementRight;
	}
}