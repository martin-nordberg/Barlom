//
// (C) Copyright 2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.graphs.io

//---------------------------------------------------------------------------------------------------------------------

//class GraphWriter {
//
//    fun writeGraphToDxl(graph: Graph): DxlTopLevel {
//        val declarations = mutableListOf<DxlDeclaration>()
//
//        for (concept in graph.concepts) {
//            val conceptType = concept.connectionsOut.stream().filter { conn -> // TODO: known UUID for has-type instead
//                conn.type.get("name").map { pv ->
//                    val s = pv.get().state
//                    s is StringPropertyValue && s.value == "has-type"
//                }.orElse(false)
//            }.findFirst().map { c ->
//                c.to
//            }
//
//            val label = DxlUuidLabel(DxlNullOrigin, concept.id)
//            val typeRef = DxlTypeRef(getFullyQualifiedName(conceptType), false)
//            val conceptRef = DxlConceptReference(label, typeRef)
//            val dxlConcept = DxlConceptDeclaration(conceptRef, properties)
//            declarations.add(DxlConnectivityDeclaration(DxlNoDocumentation, dxlConcept, DxlNoConnectionDeclaration))
//        }
//
//        return DxlTopLevel(DxlAliases(listOf()), DxlDeclarations(declarations))
//    }
//
//    private fun getFullyQualifiedName(conceptType: Optional<IConcept>): DxlName {
//        conceptType.map { ct ->
//            val n = ct.get("name")
//            if (n == null) {
//                DxlN
//            }
//        }
//    }
//
//}

//---------------------------------------------------------------------------------------------------------------------
