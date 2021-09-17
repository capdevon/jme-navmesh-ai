# jme3-recast4j-test-1
This project was born with the aim of creating a bridge API between the recast4j library and jMonkeyEngine.
 
⚠️ This library is still under active development! 
Follow me to stay updated on the improvements I am working on.
Write me your suggestions or report any bugs.

⚠️ The com.jme3.recast4j.Detour.Crowd package is in the design phase and could vary a lot with each update.

### Recently added features:
- NavMeshAgent that allows you to move the character in the Scene using the NavMesh.
- NavMeshQueryFilter - Specifies which areas to consider when searching the NavMesh.
- NavMeshTools - Use the NavMesh class to perform spatial queries such as pathfinding and walkability tests.
- NavMeshBuildMarkup allows you to control how certain objects are treated during the NavMesh build process, specifically when collecting sources for building.
- NavMeshBuildSettings allows you to specify a collection of settings which describe the dimensions and limitations of a particular agent type.
- SoloNavMeshBuilder
- TileNavMeshBuilder
- IORecast to export **NavMesh** in .obj format.
- Graphic editor to modify the NavMesh generation parameters at Runtime.

# Requirements
The individual projects and their respective requirements used in this demo can be found by following these links.

- [jmonkeyengine](https://github.com/jMonkeyEngine/jmonkeyengine) - A complete 3D game development suite written purely in Java.
- [recast4j](https://github.com/ppiastucki/recast4j) - Java Port of Recast & Detour navigation mesh toolset.
- [Lemur](https://github.com/jMonkeyEngine-Contributions/Lemur) - jMonkeyEngine-based UI toolkit.
- java 11+

The gradle dependencies are as follows.

Gradle
```
ext.jmeVersion = '3.4.0-stable'
ext.recast4jVersion = '1.2.8'

dependencies {
    // Engine
    implementation 'org.jmonkeyengine:jme3-core:' + jmeVersion
    implementation 'org.jmonkeyengine:jme3-desktop:' + jmeVersion
    implementation 'org.jmonkeyengine:jme3-effects:' + jmeVersion
    implementation 'org.jmonkeyengine:jme3-terrain:' + jmeVersion
    implementation 'org.jmonkeyengine:jme3-jbullet:' + jmeVersion
    runtimeOnly 'org.jmonkeyengine:jme3-lwjgl:' + jmeVersion
    runtimeOnly 'org.jmonkeyengine:jme3-jogg:' + jmeVersion
    runtimeOnly 'org.jmonkeyengine:jme3-plugins:' + jmeVersion
    
    // recast4j
    implementation 'org.recast4j:parent:' + recast4jVersion
    implementation 'org.recast4j:detour-tile-cache:' + recast4jVersion
    implementation 'org.recast4j:detour-crowd:' + recast4jVersion
    implementation 'org.recast4j:detour-extras:' + recast4jVersion
    implementation 'org.recast4j:recast:' + recast4jVersion
    implementation 'org.recast4j:detour:' + recast4jVersion
        
    // Lemur GUI and Groovy:
    implementation 'com.simsilica:lemur:1.15.0'
    implementation 'com.simsilica:lemur-props:1.1.1'
    implementation 'com.simsilica:lemur-proto:1.12.0'
    runtimeOnly 'org.codehaus.groovy:groovy-jsr223:3.0.8'
}
```

# Documentation
- [jme3-recast4j](https://github.com/MeFisto94/jme3-recast4j-demo/wiki)
- [Recast Navigation for JME](https://wiki.jmonkeyengine.org/docs/3.4/contributions/ai/recast.html)
- [Building Process Slides](https://github.com/capdevon/jme3-recast4j-test-1/blob/main/docs/MikkoMononen_RecastSlides.pdf) - A series of slides that explain the build process of Recast.

# Youtube videos
[Demo Navigation-mesh](https://www.youtube.com/watch?v=XVWxy30IH7Q)

[Demo Agent-based Crowd Simulation](https://www.youtube.com/watch?v=0yvne6mQVy4)

# 
⭐NEW Editor
![Screenshot](images/editor-2.jpg)
Solo NavMesh
![Screenshot](images/buildSoloModified-2.jpg)
IORecast export .obj
![Screenshot](images/navmesh-obj-export.jpg)

# Credits
Huge thanks to MeFisto94 ad mitm001 for writing the original project this is based on.
