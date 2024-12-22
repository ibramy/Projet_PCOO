package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;


public class MyGdxGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture img;
    Texture img_bullet;
    Texture img_alien;
    Player player;
    Alien[] aliens;
    int NumWidth_aliens = 8;
    int NumHeight_aliens = 4;
    int spacing_aliens = 65;
    int minX_aliens;
    int minY_aliens;
    int maxX_aliens;
    int maxY_aliens;
    int direction_aliens = 1;
    float speed_aliens = 100;
    TiledMap tiledMap;
    OrthogonalTiledMapRenderer tiledMapRenderer;
    OrthographicCamera camera;
    


    // offset to move the aliens
    Vector2 offset_aliens;

    // Variable pour vérifier si le joueur est mort
    boolean playerIsDead = false;
    

    @Override
    public void create () {
        tiledMap = new TmxMapLoader().load("starfield.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        float mapWidth = tiledMap.getProperties().get("width",Integer.class) * tiledMap.getProperties().get("tilewidth", Integer.class);
        float mapHeight = tiledMap.getProperties().get("height",Integer.class) * tiledMap.getProperties().get("tileheight", Integer.class);
        camera.position.set(mapWidth/2f,mapHeight/2f,0);
        camera.update();

        offset_aliens = Vector2.Zero;
        batch = new SpriteBatch();
        img = new Texture("player.png");
        img_bullet = new Texture("bullet.png");
        img_alien = new Texture("alien.png");
        player = new Player(img, img_bullet);
        aliens = new Alien[NumWidth_aliens * NumHeight_aliens];
        int i = 0;
        for(int y = 0; y < NumHeight_aliens; y++) {
            for(int x = 0; x < NumWidth_aliens; x++) {
                Vector2 position = new Vector2(x * spacing_aliens, y * spacing_aliens);
                position.x += Gdx.graphics.getWidth() / 2;
                position.y += Gdx.graphics.getWidth();
                position.x -= (NumWidth_aliens / 2) * spacing_aliens;
                position.y -= (NumHeight_aliens) * spacing_aliens;
                aliens[i] = new Alien(position, img_alien);
                i++;
            }
        }
    }

    int amount_alive_aliens = 0;

    @Override
    public void render () {
        float deltaTime = Gdx.graphics.getDeltaTime();
        camera.update();
        tiledMapRenderer.setView(camera);
        ScreenUtils.clear(0, 0, 0, 1);
        tiledMapRenderer.render();

        
        batch.begin();
        
        

        // Vérifier si le joueur est mort
        if (!playerIsDead) {
            player.Draw(batch);
        } else {
            // Afficher un message de Game Over
            // Tu pourrais utiliser un Label ou un autre moyen de notification ici
            batch.draw(new Texture("game_over.jpg"), Gdx.graphics.getWidth() / 2 -225, Gdx.graphics.getHeight() / 2); // Un exemple de message
        }

        for(int i = 0; i < aliens.length; i++) {
            if(aliens[i].Alive) {
                if(player.sprite_bullet.getBoundingRectangle().overlaps(aliens[i].sprite.getBoundingRectangle())) {
                    player.position_bullet.y = 10000;
                    aliens[i].Alive = false;
                    break;
                }
            }
        }

        minX_aliens = 10000;
        minY_aliens = 10000;
        maxX_aliens = 0;
        maxY_aliens = 0;
        amount_alive_aliens = 0;

        for(int i = 0; i < aliens.length; i++) {
            if(aliens[i].Alive) {
                int IndexX = i % NumWidth_aliens;
                int IndexY = i / NumWidth_aliens;
                if(IndexX > maxX_aliens) maxX_aliens = IndexX;
                if(IndexX < minX_aliens) minX_aliens = IndexX;
                if(IndexY > maxY_aliens) maxY_aliens = IndexY;
                if(IndexY < minY_aliens) minY_aliens = IndexY;
                amount_alive_aliens++;
            }
        }

        if(amount_alive_aliens == 0) {
            for(int i = 0; i < aliens.length; i++) {
                aliens[i].Alive = true;
            }
            offset_aliens = new Vector2(0, 0);
            batch.end();
            speed_aliens = 100;
            return;
        }

        offset_aliens.x += direction_aliens * deltaTime * speed_aliens;
        if(aliens[maxX_aliens].position.x >= Gdx.graphics.getWidth()) {
            direction_aliens = -1;
            offset_aliens.y -= aliens[0].sprite.getHeight() * aliens[0].sprite.getScaleY() * 0.25f;
            speed_aliens += 3;
        }
        if(aliens[minX_aliens].position.x <= 0) {
            direction_aliens = 1;
            offset_aliens.y -= aliens[0].sprite.getHeight() * aliens[0].sprite.getScaleY() * 0.25f;
            speed_aliens += 3;
        }
        
        for(int i = 0; i < aliens.length; i++) {
            aliens[i].position = new Vector2(aliens[i].position_initial.x + offset_aliens.x, aliens[i].position_initial.y + offset_aliens.y);
            if(aliens[i].Alive) {
                aliens[i].Draw(batch);

                // Vérifier si un alien touche le joueur
                if(aliens[i].sprite.getBoundingRectangle().overlaps(player.sprite.getBoundingRectangle())) {
                    playerIsDead = true; // Le joueur meurt
                }
            }
            
        }
        batch.end();     
    }
   
    @Override
    public void dispose () {
        batch.dispose();
        img.dispose();
        tiledMap.dispose();
        tiledMapRenderer.dispose();
    }
}

