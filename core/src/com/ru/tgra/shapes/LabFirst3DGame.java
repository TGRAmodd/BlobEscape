package com.ru.tgra.shapes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;

public class LabFirst3DGame extends ApplicationAdapter {

	Shader shader;
	
	private float angle;
	private float objectRotationAngle;

	public static int colorLoc;
	
	private Camera cam;
	private Camera orthoCam;
	
	private float fov = 100.0f;
	
	private Maze maze;
	
	private Sound sound;
	private Sound winSong;
	private boolean win;
	private long winTrack;
	private long track;
	private float volume;

	@Override
	public void create () 
	{
		win = true;
		volume = 1;
		
		shader = new Shader();
		maze = new Maze(15, 15);

		BoxGraphic.create(shader.getVertexPointer(), shader.getNormalPointer());
		SphereGraphic.create(shader.getVertexPointer(), shader.getNormalPointer());
		SincGraphic.create(shader.getVertexPointer());
		CoordFrameGraphic.create(shader.getVertexPointer());

		Gdx.gl.glClearColor(0.3f, 0.3f, 0.8f, 1.0f);

		ModelMatrix.main = new ModelMatrix();
		ModelMatrix.main.loadIdentityMatrix();
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		cam = new Camera();
		cam.look(new Point3D(1.5f, 1f, -0.5f), new Point3D(2.5f,1,-1.5f), new Vector3D(0,1,0));
		orthoCam = new Camera();
		orthoCam.orthographicProjection(-10, 10, -10, 10, 3.0f, 100);
		
		Gdx.input.setCursorCatched(true);
		sound = Gdx.audio.newSound(Gdx.files.internal("hall.mp3"));
		winSong = Gdx.audio.newSound(Gdx.files.internal("celebrate.mp3"));
		track = sound.play(1);
	}
	
	private void update()
	{
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		/* If we have reached the area where the z coord is less than -13 then we have reached 
		 * our end goal, quit playing our stressful music and start playing celebratory music */
		if(cam.eye.z < -13 && win){
			win = false;
			sound.dispose();
			winTrack = winSong.play(1);
		}
		
		angle += 180.0f * deltaTime;
		
		if(Gdx.input.isKeyPressed(Input.Keys.A)) {
			cam.slide(-3.0f * deltaTime, 0, 0);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.D)) {
			cam.slide(3.0f * deltaTime, 0, 0);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.W)) {
			cam.walkForward(3.0f * deltaTime);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.S)) {
			cam.walkForward(-3.0f * deltaTime);
		}
		
		/* Mute functionality */
		if(Gdx.input.isKeyJustPressed(Input.Keys.M)) {
			if(volume == 1){
				volume = 0;
			}
			else{
				volume = 1;
			}
			if(win){
				sound.setVolume(track, volume);
			}
			else{
				winSong.setVolume(winTrack, volume);
			}
		}
		
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			cam.rotateY(90.0f * deltaTime);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			cam.rotateY(-90.0f * deltaTime);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
			cam.pitch(90.0f * deltaTime);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			cam.pitch(-90.0f * deltaTime);
		}
		
		if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
			Gdx.graphics.setDisplayMode(500, 500, false);
			if(!win){
				winSong.dispose();
			}else{
				sound.dispose();
			}
			Gdx.app.exit();
		}
		
		cam.rotateY(-0.2f * Gdx.input.getDeltaX());
		cam.pitch(-0.2f * Gdx.input.getDeltaY());		
	}
	
	private void display()
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glUniform4f(LabFirst3DGame.colorLoc, 1.0f, 0.3f, 0.1f, 1.0f);
		for(int viewNum = 0; viewNum < 2; viewNum++)
		{
			if(viewNum == 0)
			{
				Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight());
				cam.perspectiveProjection(fov, 1.0f, 0.1f, 100.0f);
				shader.setViewMatrix(cam.getViewMatrix());
				shader.setProjectionMatrix(cam.getProjectionMatrix());
				shader.setEyePosition(cam.eye.x, cam.eye.y, cam.eye.z, 1.0f);
			}
			else
			{
				Gdx.gl.glViewport(Gdx.graphics.getWidth() / 2, 0, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight());
				orthoCam.look(new Point3D(7.0f, 40.0f, -7.0f), new Point3D(7.0f, 0.0f, -7.0f), new Vector3D(0,0,-1));
				shader.setViewMatrix(orthoCam.getViewMatrix());
				shader.setProjectionMatrix(orthoCam.getProjectionMatrix());
				shader.setEyePosition(orthoCam.eye.x, orthoCam.eye.y, orthoCam.eye.z, 1.0f);
			}
		
			ModelMatrix.main.loadIdentityMatrix();
			
			float s = (float)Math.sin(angle * Math.PI / 180.0);
			float c = (float)Math.cos(angle * Math.PI / 180.0);
			

			//Light 1
			shader.setLightPosition(4 * s + 10.0f, 7.0f,4 * c -10.0f, 1.0f);
			shader.setLightColor(1.0f, 0.3f, 0.3f, 1.0f);

			//Light 2
			shader.setLightPosition2(4 * s + 4.0f, 7.0f, 4 * c - 6.0f, 1.0f);
			shader.setLightColor2(0.3f, 1.0f, 0.3f, 1.0f);


			//Light 3
			shader.setLightPosition3(4 * s + 8.0f, 7.0f, 4 * c -1.0f, 1.0f);
			shader.setLightColor3(0.3f, 0.3f, 1.0f, 1.0f);

			//Directional light
			shader.setLightColor4(0.2f, 0.2f, 0.2f, 1.0f);
			
			
			shader.setGlobalAmbient(0.2f, 0.2f, 0.2f, 1);
			shader.setMaterialEmission(1.0f, 1.0f, 1.0f, 1.0f);
			shader.setMaterialDiffuse(0, 0, 0, 1);
			shader.setMaterialSpecular(0, 0, 0, 1);
			
			//Lightbulp 1
			ModelMatrix.main.pushMatrix();
			ModelMatrix.main.addTranslation(4 * s + 10.0f, 7.0f,4 * c -10.0f);
			ModelMatrix.main.addScale(0.1f, 0.1f, 0.1f);
			shader.setModelMatrix(ModelMatrix.main.getMatrix());
			SphereGraphic.drawSolidSphere();
			ModelMatrix.main.popMatrix();
			
			//Lightbulp 2
			ModelMatrix.main.pushMatrix();
			ModelMatrix.main.addTranslation(4 * s + 4.0f, 7.0f, 4 * c - 6.0f);
			ModelMatrix.main.addScale(0.1f, 0.1f, 0.1f);
			shader.setModelMatrix(ModelMatrix.main.getMatrix());
			SphereGraphic.drawSolidSphere();
			ModelMatrix.main.popMatrix();
			
			//Lightbulp 3
			ModelMatrix.main.pushMatrix();
			ModelMatrix.main.addTranslation(4 * s + 8.0f, 7.0f, 4 * c - 1.0f);
			ModelMatrix.main.addScale(0.1f, 0.1f, 0.1f);
			shader.setModelMatrix(ModelMatrix.main.getMatrix());
			SphereGraphic.drawSolidSphere();
			ModelMatrix.main.popMatrix();
			
			shader.setMaterialDiffuse(0.3f, 0.3f, 0.7f, 1.0f);
			shader.setMaterialSpecular(1.0f, 1.0f, 1.0f, 1.0f);
			shader.setMaterialEmission(0, 0, 0, 1);
			shader.setShininess(30.0f);
			
			ModelMatrix.main.pushMatrix();
			ModelMatrix.main.addTranslation(8.0f, 10.0f, -8.0f);
			shader.setModelMatrix(ModelMatrix.main.getMatrix());
			SphereGraphic.drawSolidSphere();
			ModelMatrix.main.popMatrix();

			drawExtraObjects();
				
			maze.drawMaze();
			
			if(viewNum == 1)
			{
				//shader.setMaterialDiffuse(1.0f, 0.3f, 0.1f, 1.0f);
				
				ModelMatrix.main.pushMatrix();
				ModelMatrix.main.addTranslation(cam.eye.x, cam.eye.y, cam.eye.z);

				ModelMatrix.main.addScale(0.25f, 0.25f, 0.25f);
				shader.setModelMatrix(ModelMatrix.main.getMatrix());
				SphereGraphic.drawSolidSphere();

				ModelMatrix.main.popMatrix();				
			}
		}
	}

	@Override
	public void render () {
		//put the code inside the update and display methods, depending on the nature of the code
		update();
		display();

	}
	/* A method used to draw the extra objects in our project. They are best seen
	 * when the end of the maze has been reached. The objects are a large cross
	 * that the user can't pass through, a flying box with spheres orbiting it,
	 * and two other spheres.
	 */
	public void drawExtraObjects() {
		//draw collidable object
		ModelMatrix.main.pushMatrix();
		ModelMatrix.main.addTranslation(7.5f, 1, -16.0f);

		ModelMatrix.main.addScale(0.5f, 9f, 0.5f);
		objectRotationAngle += 45 * Gdx.graphics.getDeltaTime();
		ModelMatrix.main.addRotationY(objectRotationAngle);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.popMatrix();
		
		ModelMatrix.main.pushMatrix();
		ModelMatrix.main.addTranslation(7.5f, 4, -16.0f);
		objectRotationAngle += 45 * Gdx.graphics.getDeltaTime();
		ModelMatrix.main.addRotationY(objectRotationAngle);
		ModelMatrix.main.addScale(0.5f, 0.5f, 3f);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.popMatrix();
		
		//draw floating objects
		ModelMatrix.main.pushMatrix();
		ModelMatrix.main.addTranslation(7.5f, 10.0f, -14.0f);
		ModelMatrix.main.addScale(1, 2, 1);
		objectRotationAngle += 45 * Gdx.graphics.getDeltaTime();
		ModelMatrix.main.addRotationY(objectRotationAngle);
		ModelMatrix.main.addTranslation(0, 0, 1);
		ModelMatrix.main.addRotationX(-objectRotationAngle);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		BoxGraphic.drawSolidCube();
		ModelMatrix.main.pushMatrix();
		ModelMatrix.main.addScale(0.25f, 0.25f, 0.25f);
		ModelMatrix.main.addRotationY(objectRotationAngle);
		ModelMatrix.main.addTranslation(6, 0, 0);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		SphereGraphic.drawSolidSphere();
		ModelMatrix.main.popMatrix();
		ModelMatrix.main.pushMatrix();
		ModelMatrix.main.addScale(0.25f, 0.25f, 0.25f);
		ModelMatrix.main.addRotationY(-objectRotationAngle);
		ModelMatrix.main.addTranslation(0, 0, 8);
		shader.setModelMatrix(ModelMatrix.main.getMatrix());
		SphereGraphic.drawSolidSphere();
		ModelMatrix.main.popMatrix();
		ModelMatrix.main.popMatrix();
	}
}