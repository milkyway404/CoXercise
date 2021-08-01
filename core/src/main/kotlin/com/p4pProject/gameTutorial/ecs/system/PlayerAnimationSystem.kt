package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.GdxRuntimeException
import com.p4pProject.gameTutorial.ecs.component.*
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.debug
import ktx.log.error
import ktx.log.logger
import java.util.*


private val LOG = logger<PlayerAnimationSystem>()
class PlayerAnimationSystem(
    private val atlas: TextureAtlas
): IteratingSystem(allOf(PlayerComponent::class, FacingComponent::class, GraphicComponent::class, PlayerAnimationComponent::class).get()),
    EntityListener {

    private val animationCache = EnumMap<AnimationType, Animation2D>(AnimationType::class.java)

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(family, this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val facing = entity[FacingComponent.mapper]
        require(facing != null ){"Entity |entity| must have a FacingComponent. entity=$entity"}

        val graphic = entity[GraphicComponent.mapper]
        require(graphic != null ){"Entity |entity| must have a GraphicComponent. entity=$entity"}

        val aniCmp = entity[PlayerAnimationComponent.mapper]
        require(aniCmp != null ){"Entity |entity| must have a PlayerAnimationComponent. entity=$entity"}

        val player = entity[PlayerComponent.mapper]
        require(player != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}

//        if(facing.direction == facing.lastDirection && graphic.sprite.texture!= null){
//            LOG.debug { "lol gotcha" }
//            return
//        }

        facing.lastDirection = facing.direction


        if(player.isAttacking){
            val region =  when(facing.direction){
                FacingDirection.WEST -> animateLeftAttack(aniCmp, deltaTime)
                FacingDirection.EAST -> animateRightAttack(aniCmp, deltaTime)
                FacingDirection.NORTH -> animateUpAttack(aniCmp, deltaTime)
                FacingDirection.SOUTH -> animateDownAttack(aniCmp, deltaTime)
            }
            graphic.setSpriteRegion(region)
        }else{
            val region = when(facing.direction){
                FacingDirection.WEST -> animateIdleLeft(aniCmp, deltaTime)
                FacingDirection.EAST -> animateIdleRight(aniCmp, deltaTime)
                FacingDirection.NORTH -> animateIdleUp(aniCmp, deltaTime)
                FacingDirection.SOUTH -> animateIdleDown(aniCmp, deltaTime)
            }
            graphic.setSpriteRegion(region)
        }

        /*val region = when(facing.direction){
                FacingDirection.WEST -> animateLeft(aniCmp, deltaTime)
                FacingDirection.EAST -> animateRight(aniCmp, deltaTime)
                FacingDirection.NORTH -> animateUp(aniCmp, deltaTime)
                FacingDirection.SOUTH -> animateDown(aniCmp, deltaTime)
        }



        graphic.setSpriteRegion(region)*/
    }

    override fun entityAdded(entity: Entity) {
        entity[PlayerAnimationComponent.mapper]?.let{ aniCmp ->
            aniCmp.animation = getAnimation(aniCmp.typeUp)
            val frame = aniCmp.animation.getKeyFrame(aniCmp.stateTime)
            entity[GraphicComponent.mapper]?.setSpriteRegion(frame)
        }
    }

    private fun animateIdleUp(aniCmp:PlayerAnimationComponent, deltaTime: Float): TextureRegion {
        if (aniCmp.typeUp == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeUp)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateIdleDown(aniCmp:PlayerAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeDown == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeDown)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateIdleLeft(aniCmp:PlayerAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeLeft == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeLeft)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateIdleRight(aniCmp:PlayerAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeRight == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeRight)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateRightAttack(aniCmp:PlayerAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeAttackRight == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeAttackRight)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateLeftAttack(aniCmp:PlayerAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeAttackLeft == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeAttackLeft)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }
    private fun animateUpAttack(aniCmp:PlayerAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeAttackUp == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeAttackUp)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }
    private fun animateDownAttack(aniCmp:PlayerAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeAttackDown == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeAttackDown)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }



    private fun getAnimation(type : AnimationType) : Animation2D{
        var animation = animationCache[type]
        if(animation == null){
            var regions = atlas.findRegions(type.atlasKey)
            if(regions.isEmpty){
                LOG.error { "No regions found for ${type.atlasKey}" }
                regions = atlas.findRegions("error")
                if (regions == null) throw GdxRuntimeException("There is no error region in the atlas")
            }else{
                LOG.debug{"Adding animation of type $type with ${regions.size} regions"}
            }
            animation = Animation2D(type, regions, type.playMode, type.speedRate)
            animationCache[type] = animation
        }
        return animation
    }

    override fun entityRemoved(entity: Entity?) = Unit


}