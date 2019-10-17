package managementportal

data class Subject(
    val subjectId: String,
    val projectId: String,
    val sourceTypes: List<SourceType>,
    val sourcesMetadata: List<SourceMetadata>,
    val attributes: Map<String, String>
) {
}