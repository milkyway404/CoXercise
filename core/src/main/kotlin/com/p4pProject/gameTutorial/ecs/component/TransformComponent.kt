package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class TransformComponent: Component, Pool.Poolable, Comparable<TransformComponent> {
    val position = Vector3()
    val prevPosition = Vector3()
    val interpolatedPosition = Vector3()
    val size = Vector2(1f, 1f)
    var rotationDeg = 0f

    override fun reset() {
        position.set(Vector3.Zero)
        prevPosition.set(Vector3.Zero)
        interpolatedPosition.set(Vector3.Zero)
        size.set(1f,1f)
        rotationDeg = 0f
    }

    fun setInitialPosition(x : Float, y : Float, z : Float){
        position.set(x,y,z)
        prevPosition.set(x,y,z)
        interpolatedPosition.set(x,y,z)
    }

    fun setSize(x : Float, y : Float,){
        size.set(x,y)
    }

    override fun compareTo(other: TransformComponent): Int {
        val zDiff = other.position.z.compareTo(position.z)
        return if(zDiff == 0) other.position.y.compareTo(position.y) else zDiff
    }

    private fun makeRect(transformComponent: TransformComponent): Rectangle {
        return Rectangle().set(
            transformComponent.position.x,
            transformComponent.position.y,
            transformComponent.size.x,
            transformComponent.size.y
        )
    }
    fun overlapsRect(otherRect: Rectangle): Boolean {
        val thisRect = makeRect(this)
        return thisRect.overlaps(otherRect)
    }

    fun overlaps(other: TransformComponent): Boolean {
        val thisBoundingRect = makeRect(this)

        val otherBoundingRect = makeRect(other)

        Gdx.app.log("this bounding rect", thisBoundingRect.toString())
        Gdx.app.log("other bounding rect", otherBoundingRect.toString())
        return thisBoundingRect.overlaps(otherBoundingRect);
    }

    companion object {
        val mapper = mapperFor<TransformComponent>()
    }

}