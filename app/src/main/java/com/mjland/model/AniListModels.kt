package com.mjland.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class AniListResponse(
    val data: AniListData
)

@JsonClass(generateAdapter = true)
data class AniListData(
    val Page: AniListPage?,
    val Media: AnimeDetails?,
    val airingSchedules: List<AiringSchedule>? = null,
    val GenreCollection: List<String>? = null,
    val MediaTagCollection: List<MediaTag>? = null,
    val Viewer: ViewerNode? = null,
    val MediaListCollection: MediaListCollectionNode? = null,
    val SaveMediaListEntry: SaveMediaListEntryNode? = null,
    val ToggleFavourite: ToggleFavouriteNode? = null
)

@JsonClass(generateAdapter = true)
data class AniListPage(
    val media: List<AnimeMedia>?,
    val airingSchedules: List<AiringSchedule>? = null
)

@JsonClass(generateAdapter = true)
data class AiringSchedule(
    val id: Int,
    val airingAt: Long?,
    val episode: Int?,
    val media: AnimeMedia?
)

@JsonClass(generateAdapter = true)
data class AnimeMedia(
    val id: Int,
    val idMal: Int? = null,
    val title: MediaTitle?,
    val coverImage: CoverImage?,
    val bannerImage: String?,
    val format: String?,
    val episodes: Int?,
    val status: String?,
    val description: String?,
    val averageScore: Int?,
    val genres: List<String>?,
    val tags: List<MediaTag>? = null,
    val seasonYear: Int?
)

@JsonClass(generateAdapter = true)
data class AnimeDetails(
    val id: Int,
    val idMal: Int?,
    val title: MediaTitle?,
    val coverImage: CoverImage?,
    val bannerImage: String?,
    val format: String?,
    val status: String?,
    val description: String?,
    val averageScore: Int?,
    val meanScore: Int?,
    val genres: List<String>?,
    val season: String?,
    val seasonYear: Int?,
    val episodes: Int?,
    val duration: Int?,
    val nextAiringEpisode: NextAiringEpisode?,
    val streamingEpisodes: List<StreamingEpisode>?,
    val trailer: Trailer?,
    val characters: CharacterConnection?,
    val recommendations: RecommendationConnection?,
    val relations: RelationConnection?,
    val tags: List<MediaTag>?,
    val staff: StaffConnection?,
    val popularity: Int?,
    val favourites: Int?,
    val studios: StudioConnection?,
    val mediaListEntry: MediaListEntry? = null
)

@JsonClass(generateAdapter = true)
data class StudioConnection(
    val edges: List<StudioEdge>?
)

@JsonClass(generateAdapter = true)
data class StudioEdge(
    val isMain: Boolean?,
    val node: StudioNode?
)

@JsonClass(generateAdapter = true)
data class StudioNode(
    val id: Int,
    val name: String?
)

@JsonClass(generateAdapter = true)
data class MediaTag(
    val name: String?,
    val category: String? = null,
    val isAdult: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class StaffConnection(
    val edges: List<StaffEdge>?
)

@JsonClass(generateAdapter = true)
data class StaffEdge(
    val node: StaffNode?,
    val role: String?
)

@JsonClass(generateAdapter = true)
data class StaffNode(
    val id: Int,
    val name: CharacterName?,
    val image: CharacterImage?
)

@JsonClass(generateAdapter = true)
data class RelationConnection(
    val edges: List<RelationEdge>?
)

@JsonClass(generateAdapter = true)
data class RelationEdge(
    val relationType: String?,
    val node: AnimeMedia?
)

@JsonClass(generateAdapter = true)
data class StreamingEpisode(
    val title: String?,
    val thumbnail: String?,
    val url: String?,
    val site: String?
)

@JsonClass(generateAdapter = true)
data class NextAiringEpisode(
    val episode: Int?,
    val timeUntilAiring: Long?,
    val airingAt: Long?
)

@JsonClass(generateAdapter = true)
data class Trailer(
    val id: String?,
    val site: String?,
    val thumbnail: String?
)

@JsonClass(generateAdapter = true)
data class CharacterConnection(
    val edges: List<CharacterEdge>?
)

@JsonClass(generateAdapter = true)
data class CharacterEdge(
    val node: CharacterNode?,
    val role: String?,
    val voiceActors: List<VoiceActorNode>?
)

@JsonClass(generateAdapter = true)
data class CharacterNode(
    val id: Int,
    val name: CharacterName?,
    val image: CharacterImage?
)

@JsonClass(generateAdapter = true)
data class CharacterName(
    val full: String?,
    val userPreferred: String?
)

@JsonClass(generateAdapter = true)
data class CharacterImage(
    val large: String?
)

@JsonClass(generateAdapter = true)
data class VoiceActorNode(
    val id: Int,
    val name: CharacterName?,
    val image: CharacterImage?
)

@JsonClass(generateAdapter = true)
data class RecommendationConnection(
    val nodes: List<RecommendationNode>?
)

@JsonClass(generateAdapter = true)
data class RecommendationNode(
    val mediaRecommendation: AnimeMedia?
)

@JsonClass(generateAdapter = true)
data class MediaTitle(
    val romaji: String?,
    val english: String?,
    val native: String?
)

@JsonClass(generateAdapter = true)
data class CoverImage(
    val extraLarge: String?,
    val large: String?
)

@JsonClass(generateAdapter = true)
data class ViewerNode(
    val id: Int,
    val name: String?,
    val avatar: AvatarNode?,
    val favourites: FavouritesNode? = null
)

@JsonClass(generateAdapter = true)
data class AvatarNode(
    val large: String?
)

@JsonClass(generateAdapter = true)
data class FavouritesNode(
    val anime: FavouritesAnimeConnection?
)

@JsonClass(generateAdapter = true)
data class FavouritesAnimeConnection(
    val nodes: List<AnimeMedia>?
)

@JsonClass(generateAdapter = true)
data class SaveMediaListEntryNode(
    val id: Int,
    val status: String?,
    val progress: Int?
)

@JsonClass(generateAdapter = true)
data class ToggleFavouriteNode(
    val anime: FavouritesAnimeConnection?
)

@JsonClass(generateAdapter = true)
data class MediaListCollectionNode(
    val lists: List<MediaListGroup>?
)

@JsonClass(generateAdapter = true)
data class MediaListGroup(
    val name: String?,
    val isCustomList: Boolean?,
    val isSplitCompletedList: Boolean?,
    val status: String?,
    val entries: List<MediaListEntry>?
)

@JsonClass(generateAdapter = true)
data class MediaListEntry(
    val id: Int,
    val mediaId: Int,
    val status: String?,
    val score: Double?,
    val progress: Int?,
    val updatedAt: Long?,
    val media: AnimeMedia?
)
