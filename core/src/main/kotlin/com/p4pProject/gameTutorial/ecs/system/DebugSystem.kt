package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils
import com.p4pProject.gameTutorial.ecs.component.PlayerComponent
import com.p4pProject.gameTutorial.ecs.component.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.ashley.getSystem
import java.lang.Float.min

private const val  WINDOW_INFO_UPDATE_RATE = 0.25f

class DebugSystem : IntervalIteratingSystem(allOf(PlayerComponent::class).get(), WINDOW_INFO_UPDATE_RATE){

    init {
        //set this to false to disable debug system
        setProcessing(true)
    }

    override fun processEntity(entity: Entity) {
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a TransformComponent. entity=$entity"}
        val player = entity[PlayerComponent.mapper]
        require(player != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}


        when{
            Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) -> {
                transform.position.y = 1f
                player.life = 1f
                player.shield = 0f
            }

            Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) -> {
                player.shield = kotlin.math.min(player.maxShield, player.shield + 25f)
            }

            Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) -> {
                player.shield = 0f
            }

            Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) -> {
                engine.getSystem<MoveSystem>().setProcessing(false)
            }

            Gdx.input.isKeyJustPressed(Input.Keys.NUM_5) -> {
                engine.getSystem<MoveSystem>().setProcessing(true)
            }
        }

        Gdx.graphics.setTitle("CoEx Debug - pos:${transform.position}, life:${player.life}, shield:${player.shield}")
    }
}