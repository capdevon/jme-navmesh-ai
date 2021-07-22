package com.jme3.recast4j.Detour.Crowd;

import org.recast4j.detour.crowd.CrowdAgentParams;

public class CrowdAgentParamsBuilder {
    // "mandatory"
    protected float radius;
    protected float height;
    protected float maxAcceleration;
    protected float maxSpeed;

    // "non-mandatory", we'll just use reasonable defaults.
    protected float collisionQueryRange;
    protected float pathOptimizationRange;
    protected float separationWeight;
    protected int updateFlags;
    protected int obstacleAvoidanceType;
    protected int queryFilterType;
    protected Object userData;

    /**
     * Create a new builder and initialize it with the mandatory attributes.<br />
     * @param radius The agent radius [Limit: >= 0]
     * @param height The agent height [Limit: > 0]
     * @param maxAcceleration the maximum acceleration [Limit: >= 0]
     * @param maxSpeed the maximum speed [Limit: >= 0]
     */
    // This constructor is used to initialize the "mandatory" fields
    public CrowdAgentParamsBuilder(float radius, float height, float maxAcceleration, float maxSpeed) {
        this.radius = radius;
        this.height = height;
        this.maxAcceleration = maxAcceleration;
        this.maxSpeed = maxSpeed;
    }

    // This constructor is used to initialize the defaults for "non-mandatory" fields, like the indices and ranges
    protected CrowdAgentParamsBuilder() {
        collisionQueryRange = 12f;
        pathOptimizationRange = 30f;
        separationWeight = 1f;
        updateFlags = 0;
        obstacleAvoidanceType = 0;
        queryFilterType = 0;
        userData = null;
    }

    /**
     * Sets the agent radius [Limit: >= 0]
     * @param radius the radius
     * @return this (fluent interface)
     */
    public CrowdAgentParamsBuilder withRadius(float radius) {
        this.radius = radius;
        return this;
    }

    /**
     * Sets the agent height [Limit: > 0]
     * @param height the height
     * @return this (fluent interface)
     */
    public CrowdAgentParamsBuilder withHeight(float height) {
        this.height = height;
        return this;
    }

    /**
     * Sets the maximum allowed acceleration [Limit: >= 0]
     * @param maxAcceleration the acceleration
     * @return this (fluent interface)
     */
    public CrowdAgentParamsBuilder withMaxAcceleration(float maxAcceleration) {
        this.maxAcceleration = maxAcceleration;
        return this;
    }

    /**
     * Sets the maximum allowed speed [Limit: >= 0]
     * @param maxSpeed the speed
     * @return this (fluent interface)
     */
    public CrowdAgentParamsBuilder withMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
        return this;
    }

    /**
     * Defines how close a collision element must be before it is considered for steering behaviors. [Limits: > 0]
     * @param collisionQueryRange the range
     * @return this (fluent interface)
     */
    public CrowdAgentParamsBuilder withCollisionQueryRange(float collisionQueryRange) {
        this.collisionQueryRange = collisionQueryRange;
        return this;
    }

    /**
     * The path visibility optimization range. [Limit: > 0]
     * @param pathOptimizationRange the range
     * @return this (fluent interface)
     */
    public CrowdAgentParamsBuilder withPathOptimizationRange(float pathOptimizationRange) {
        this.pathOptimizationRange = pathOptimizationRange;
        return this;
    }

    /**
     * How aggressive the agent manager should be at avoiding collisions with this agent. [Limit: >= 0]
     * @param separationWeight the weight
     * @return this (fluent interface)
     */
    public CrowdAgentParamsBuilder withSeparationWeight(float separationWeight) {
        this.separationWeight = separationWeight;
        return this;
    }

    /**
     * Flags that impact steering behavior. (See: #UpdateFlags)<br />
     * Constants: {@link CrowdAgentParams#DT_CROWD_ANTICIPATE_TURNS} et al.<br />
     * This method sets the flags field directly, but there also is a counterpart to set one or more flags instead,
     * which is recommended and more readable.
     *
     * @param updateFlags The Value of the flags field.
     * @return this (fluent interface)
     * @see #withUpdateFlag(int)
     */
    public CrowdAgentParamsBuilder setUpdateFlags(int updateFlags) {
        this.updateFlags = updateFlags;
        return this;
    }

    /**
     * Flags that impact steering behavior. (See: #UpdateFlags)<br />
     * Constants: {@link CrowdAgentParams#DT_CROWD_ANTICIPATE_TURNS} et al.<br />
     * This method marks the passed flag(s) as set (internal bitwise or), but there also is a counterpart to directly
     * set the Flags field.
     * @param flag The Flag(s) to set
     * @return this (fluent interface)
     * @see #setUpdateFlags(int)
     */
    public CrowdAgentParamsBuilder withUpdateFlag(int flag) {
        this.updateFlags |= flag;
        return this;
    }

    /**
     * The index of the avoidance configuration to use for the agent.
     * [Limits: 0 <= value < #DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS]
     * @param obstacleAvoidanceType the index
     * @return this (fluent interface)
     */
    public CrowdAgentParamsBuilder withObstacleAvoidanceIndex(int obstacleAvoidanceType) {
        this.obstacleAvoidanceType = obstacleAvoidanceType;
        return this;
    }

    /**
     * The index of the query filter used by this agent.
     * @param queryFilterType the index
     * @return this (fluent interface)
     */
    public CrowdAgentParamsBuilder withQueryFilterIndex(int queryFilterType) {
        this.queryFilterType = queryFilterType;
        return this;
    }

    /**
     * User defined data attached to the agent.
     * @param userData The UserData
     * @return this (fluent interface)
     */
    public CrowdAgentParamsBuilder withUserData(Object userData) {
        this.userData = userData;
        return this;
    }

    public CrowdAgentParams build() {
        CrowdAgentParams params = new CrowdAgentParams();
        params.radius = radius;
        params.height = height;
        params.maxAcceleration = maxAcceleration;
        params.maxSpeed = maxSpeed;

        params.collisionQueryRange = collisionQueryRange;
        params.pathOptimizationRange = pathOptimizationRange;
        params.separationWeight = separationWeight;
        params.updateFlags = updateFlags;
        params.obstacleAvoidanceType = obstacleAvoidanceType;
        params.queryFilterType = queryFilterType;
        params.userData = userData;

        return params;
    }

    @Override
    public String toString() {
        return "CrowdAgentParamsBuilder{" +
                "radius=" + radius +
                ", height=" + height +
                ", maxAcceleration=" + maxAcceleration +
                ", maxSpeed=" + maxSpeed +
                ", collisionQueryRange=" + collisionQueryRange +
                ", pathOptimizationRange=" + pathOptimizationRange +
                ", separationWeight=" + separationWeight +
                ", updateFlags=" + updateFlags +
                ", obstacleAvoidanceType=" + obstacleAvoidanceType +
                ", queryFilterType=" + queryFilterType +
                ", userData=" + userData +
                '}';
    }
}
