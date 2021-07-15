package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.p4pProject.gameTutorial.ecs.component.AttachComponent
import com.p4pProject.gameTutorial.ecs.component.GraphicComponent
import com.p4pProject.gameTutorial.ecs.component.RemoveComponent
import com.p4pProject.gameTutorial.ecs.component.TransformComponent
import ktx.ashley.addComponent
import ktx.ashley.allOf
import ktx.ashley.get

class AttachSystem : EntityListener, IteratingSystem(allOf(AttachComponent::class, GraphicComponent::class, TransformComponent::class).get()){

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun entityAdded(entity: Entity)=Unit

    override fun entityRemoved(removedEntity: Entity) {
        entities.forEach { entity ->
            entity[AttachComponent.mapper]?.let{attach ->
                if (attach.entity == removedEntity){
                    entity.addComponent<RemoveComponent>(engine)
                }
            }
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val graphic = entity[GraphicComponent.mapper]
        require(graphic != null ){"Entity |entity| must have a GraphicComponent. entity=$entity"}
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a TransformComponent. entity=$entity"}
        val attach = entity[AttachComponent.mapper]
        require(attach != null ){"Entity |entity| must have a AttachComponent. entity=$entity"}

        // update position of attached entity relative to the main entity
        attach.entity[TransformComponent.mapper]?.let { attachTransform ->
            transform.interpolatedPosition.set(
                attachTransform.interpolatedPosition.x + attach.offset.x,
                attachTransform.interpolatedPosition.y + attach.offset.y,
                transform.position.z
            )
        }

        //update graphic alpha value (transparency)
        attach.entity[GraphicComponent.mapper]?.let { attachGraphic->
            graphic.sprite.setAlpha(attachGraphic.sprite.color.a)
        }
    }
}