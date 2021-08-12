package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.event.*
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get
import ktx.graphics.use
import ktx.log.error
import ktx.log.logger
import kotlin.math.min


private val LOG = logger<RenderSystem>()
class RenderSystem(
    private val batch: Batch,
    private val gameViewport: Viewport,
    private val uiViewport: Viewport,
    private val backgroundViewport: Viewport,
    backgroundTexture: Texture,
    private val gameEventManager: GameEventManager,
    private val outlineShader: ShaderProgram
) : GameEventListener, SortedIteratingSystem(
    allOf(TransformComponent::class, GraphicComponent::class).get(),
    compareBy { entity -> entity[TransformComponent.mapper] }
) {
    private val background = Sprite(backgroundTexture.apply {
        setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
    })

    private val backgroundScrollSpeed = Vector2(0.03f, -0.25f)

    private val textureSizeLocation = outlineShader.getUniformLocation("u_textureSize")
    private val outlineColorLoc = outlineShader.getUniformLocation("u_outlineColor")
    private val outlineColor = Color(0f, 113f/255f, 214/255f, 1f)
    private val playerEntities by lazy {
        engine.getEntitiesFor(allOf(PlayerComponent::class).exclude(RemoveComponent::class).get())
    }


    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addListener(GameEvent.CollectPowerUp::class, this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeListener(GameEvent.CollectPowerUp::class, this)
    }
    override fun update(deltaTime: Float) {
        backgroundViewport.apply()
        batch.use(backgroundViewport.camera.combined) {
            background.draw(batch)
        }

        forceSort()
        gameViewport.apply()
        batch.use(gameViewport.camera.combined) {
            super.update(deltaTime)
        }

        // render outlines of entities
        renderEntityOutlines()
    }

    private fun renderEntityOutlines() {
        batch.use (gameViewport.camera.combined){
            it.shader = outlineShader
            playerEntities.forEach{ entity ->
                renderPlayerOutlines(entity, it)
            }

            it.shader = null
        }
    }

    private fun renderPlayerOutlines(entity: Entity, it: Batch) {
        val player = entity[PlayerComponent.mapper]
        require(player != null){"Entity |entity| must have a PlayerComponent. entity=$entity"}

//        if(player.shield > 0f){
//            outlineColor.a = MathUtils.clamp(player.shield/player.maxShield, 0f, 1f)
//
//            outlineShader.setUniformf(outlineColorLoc, outlineColor)
//            entity[GraphicComponent.mapper]?.let { graphic ->
//                graphic.sprite.run {
//                    outlineShader.setUniformf(textureSizeLocation, texture.width.toFloat(), texture.height.toFloat())
//                    draw(batch)
//                }
//            }
//        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null){"Entity |entity| must have a TransformComponent. entity=$entity"}

        val graphic = entity[GraphicComponent.mapper]
        require(graphic != null){"Entity |entity| must have a GraphicComponent. entity=$entity"}

        if(graphic.isBackground){
            return
        }

        if(graphic.sprite.texture == null && !graphic.isBackground){
            LOG.error{"Entity has no texture for rendering. entity=$entity"}
            return
        }

        graphic.sprite.run {
            rotation = transform.rotationDeg
            setBounds(transform.interpolatedPosition.x, transform.interpolatedPosition.y, transform.size.x, transform.size.y)
            draw(batch)
        }
    }

    override fun onEvent(event: GameEvent) {
        val powerUpEvent = event as GameEvent.CollectPowerUp
    }
}