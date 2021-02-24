//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.impl

import barlom.graphs.EMetaLevel
import barlom.graphs.IConcept
import barlom.graphs.IConnection
import barlom.graphs.IGraph
import barlom.util.Uuid
import java.time.Instant


//---------------------------------------------------------------------------------------------------------------------

class Graph : IGraph {

    val anyConcept: Concept

    val anyConnection: Concept

    val barlomNs: Concept

    val concepts: MutableList<Concept> = mutableListOf()

    val conceptType: Concept

    val connections: MutableList<Connection> = mutableListOf()

    val connectionType: Concept

    val contains: Concept

    val hasType: Concept

    val inherits: Concept

    val metaConcept: Concept

    val namedConcept: Concept

    val namespace: Concept

    val rootNs: Concept

    init {

        val metaStart = Instant.EPOCH
        var uuid = Uuid.fromString("17059100-763f-11eb-bc98-02424adfd703")

        metaConcept = Concept(uuid++, EMetaLevel.SYSTEM_META_META)
        metaConcept.addProperty("name", metaStart, "MetaConcept")
        concepts.add(metaConcept)

        hasType = Concept(uuid++, EMetaLevel.SYSTEM_META)
        hasType.addProperty("name", metaStart, "has-type")
        concepts.add(hasType)

        establishConnection(uuid++, metaConcept, hasType, metaConcept)

        conceptType = establishConcept(uuid++, metaConcept, uuid++) {
            addProperty("name", metaStart, "ConceptType")
        }

        connectionType = establishConcept(uuid++, metaConcept, uuid++) {
            addProperty("name", metaStart, "ConnectionType")
        }

        establishConnection(uuid++, hasType, hasType, connectionType)

        anyConcept = establishConcept(uuid++, conceptType, uuid++) {
            addProperty("name", metaStart, "AnyConcept")
        }

        anyConnection = establishConcept(uuid++, conceptType, uuid++) {
            addProperty("name", metaStart, "AnyConnection")
        }

        contains = establishConcept(uuid++, connectionType, uuid++) {
            addProperty("name", metaStart, "contains")
        }

        inherits = establishConcept(uuid++, connectionType, uuid++) {
            addProperty("name", metaStart, "inherits")
        }

        namespace = establishConcept(uuid++, conceptType, uuid++) {
            addProperty("name", metaStart, "Namespace")
        }

        namedConcept = establishConcept(uuid++, conceptType, uuid++) {
            addProperty("name", metaStart, "NamedConcept")
        }

        rootNs = establishConcept(uuid++, namespace, uuid++) {
            addProperty("name", metaStart, "$")
        }

        barlomNs = establishConcept(uuid++, namespace, uuid++) {
            addProperty("name", metaStart, "barlom")
        }

        establishConnection(uuid++, rootNs, contains, barlomNs)
        establishConnection(uuid++, barlomNs, contains, metaConcept)
        establishConnection(uuid++, barlomNs, contains, conceptType)
        establishConnection(uuid++, barlomNs, contains, connectionType)
        establishConnection(uuid++, barlomNs, contains, hasType)
        establishConnection(uuid++, barlomNs, contains, anyConcept)
        establishConnection(uuid++, barlomNs, contains, anyConnection)
        establishConnection(uuid++, barlomNs, contains, contains)
        establishConnection(uuid++, barlomNs, contains, inherits)
        establishConnection(uuid++, barlomNs, contains, namespace)
        establishConnection(uuid++, barlomNs, contains, namedConcept)

        establishConnection(uuid++, namedConcept, inherits, anyConcept)
        establishConnection(uuid++, namespace, inherits, namedConcept)

        establishConnection(uuid++, contains, inherits, anyConnection)
        establishConnection(uuid++, hasType, inherits, anyConnection)
        establishConnection(uuid++, inherits, inherits, anyConnection)

        check(uuid.toString() == "17059128-763f-11eb-bc98-02424adfd703") { uuid.toString() }

    }

    override fun establishConcept(
        id: Uuid?,
        type: IConcept,
        typeConnectionId: Uuid?,
        initialize: IConcept.() -> Unit
    ): Concept {

        val result = Concept(id ?: Uuid.make(), type.metaLevel.nextLevelDown())

        connections.add(Connection(typeConnectionId ?: Uuid.make(), result, hasType, type as Concept))

        result.initialize()

        concepts.add(result)

        return result

    }

    override fun establishConnection(
        id: Uuid?,
        from: IConcept,
        type: IConcept,
        to: IConcept,
        initialize: IConnection.() -> Unit
    ): Connection {

        val result = Connection(id, from as Concept, type as Concept, to as Concept)

        result.initialize()

        connections.add(result)

        return result

    }

}

//---------------------------------------------------------------------------------------------------------------------
