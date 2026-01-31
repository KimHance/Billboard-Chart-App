package com.hancekim.billboard.core.domain

import com.hancekim.billboard.core.data.model.ContentDetails
import com.hancekim.billboard.core.data.model.ContentRating
import com.hancekim.billboard.core.data.model.RegionRestriction
import com.hancekim.billboard.core.data.model.SearchResult
import com.hancekim.billboard.core.data.model.SearchResultId
import com.hancekim.billboard.core.data.model.Snippet
import com.hancekim.billboard.core.data.model.Thumbnail
import com.hancekim.billboard.core.data.model.Thumbnails
import com.hancekim.billboard.core.data.model.VideoItem
import com.hancekim.billboard.core.data.model.VideoListResponse
import com.hancekim.billboard.core.data.model.YoutubeSearchResponse
import com.hancekim.billboard.core.data.repository.YoutubeRepository
import com.hancekim.billboard.core.data.exception.YoutubeException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetYoutubeVideoDetailUseCaseTest {

    private lateinit var repository: YoutubeRepository
    private lateinit var useCase: GetYoutubeVideoDetailUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetYoutubeVideoDetailUseCase(repository)
    }

    @Test
    fun `검색 결과의 videoId로 영상 정보를 조회한다`() = runTest {
        coEvery { repository.searchVideo(any()) } returns searchResponse("VIDEO_ID")
        coEvery { repository.getVideoInfo("VIDEO_ID") } returns videoResponse()

        useCase("title", "artist")

        coVerify { repository.searchVideo("title - artist") }
        coVerify { repository.getVideoInfo("VIDEO_ID") }
    }

    @Test
    fun `제한 없는 영상은 재생 가능하다`() = runTest {
        coEvery { repository.searchVideo(any()) } returns searchResponse("V1")
        coEvery { repository.getVideoInfo("V1") } returns videoResponse()

        val result = useCase("title", "artist")

        assertTrue(result.isPlayable)
    }

    @Test
    fun `KR이 blocked에 포함되면 재생 불가하다`() = runTest {
        coEvery { repository.searchVideo(any()) } returns searchResponse("V1")
        coEvery { repository.getVideoInfo("V1") } returns videoResponse(
            regionRestriction = RegionRestriction(blocked = listOf("KR", "JP"))
        )

        val result = useCase("title", "artist")

        assertFalse(result.isPlayable)
    }

    @Test
    fun `allowed 목록에 KR이 없으면 재생 불가하다`() = runTest {
        coEvery { repository.searchVideo(any()) } returns searchResponse("V1")
        coEvery { repository.getVideoInfo("V1") } returns videoResponse(
            regionRestriction = RegionRestriction(allowed = listOf("US", "JP"))
        )

        val result = useCase("title", "artist")

        assertFalse(result.isPlayable)
    }

    @Test
    fun `allowed 목록에 KR이 있으면 재생 가능하다`() = runTest {
        coEvery { repository.searchVideo(any()) } returns searchResponse("V1")
        coEvery { repository.getVideoInfo("V1") } returns videoResponse(
            regionRestriction = RegionRestriction(allowed = listOf("KR", "US"))
        )

        val result = useCase("title", "artist")

        assertTrue(result.isPlayable)
    }

    @Test
    fun `kmrbTeen 등급이면 재생 불가하다`() = runTest {
        coEvery { repository.searchVideo(any()) } returns searchResponse("V1")
        coEvery { repository.getVideoInfo("V1") } returns videoResponse(
            contentRating = ContentRating(kmrbRating = "kmrbTeen")
        )

        val result = useCase("title", "artist")

        assertFalse(result.isPlayable)
    }

    @Test
    fun `kmrbR 등급이면 재생 불가하다`() = runTest {
        coEvery { repository.searchVideo(any()) } returns searchResponse("V1")
        coEvery { repository.getVideoInfo("V1") } returns videoResponse(
            contentRating = ContentRating(kmrbRating = "kmrbR")
        )

        val result = useCase("title", "artist")

        assertFalse(result.isPlayable)
    }

    @Test
    fun `thumbnailUrl은 high 썸네일 URL이다`() = runTest {
        coEvery { repository.searchVideo(any()) } returns searchResponse(
            videoId = "V1",
            thumbnailUrl = "https://img.youtube.com/vi/V1/hqdefault.jpg"
        )
        coEvery { repository.getVideoInfo("V1") } returns videoResponse()

        val result = useCase("title", "artist")

        assertEquals("https://img.youtube.com/vi/V1/hqdefault.jpg", result.thumbnailUrl)
        assertEquals("V1", result.videoId)
    }

    @Test
    fun `영상 조회 시 404이면 VideoNotFound 예외가 발생한다`() = runTest {
        coEvery { repository.searchVideo(any()) } returns searchResponse("V1")
        coEvery { repository.getVideoInfo("V1") } throws YoutubeException.VideoNotFound("not found")

        val exception = assertThrows(YoutubeException.VideoNotFound::class.java) {
            runBlocking { useCase("title", "artist") }
        }
        assertEquals("not found", exception.message)
    }

    @Test
    fun `영상 조회 시 403이면 VideoForbidden 예외가 발생한다`() = runTest {
        coEvery { repository.searchVideo(any()) } returns searchResponse("V1")
        coEvery { repository.getVideoInfo("V1") } throws YoutubeException.VideoForbidden("forbidden")

        val exception = assertThrows(YoutubeException.VideoForbidden::class.java) {
            runBlocking { useCase("title", "artist") }
        }
        assertEquals("forbidden", exception.message)
    }

    @Test
    fun `영상 조회 시 400이면 VideoChartNotFound 예외가 발생한다`() = runTest {
        coEvery { repository.searchVideo(any()) } returns searchResponse("V1")
        coEvery { repository.getVideoInfo("V1") } throws YoutubeException.VideoChartNotFound("chart not found")

        val exception = assertThrows(YoutubeException.VideoChartNotFound::class.java) {
            runBlocking { useCase("title", "artist") }
        }
        assertEquals("chart not found", exception.message)
    }

    private fun searchResponse(
        videoId: String,
        thumbnailUrl: String = "",
    ) = YoutubeSearchResponse(
        items = listOf(
            SearchResult(
                id = SearchResultId(videoId = videoId),
                snippet = Snippet(
                    thumbnails = Thumbnails(
                        high = Thumbnail(url = thumbnailUrl)
                    )
                )
            )
        )
    )

    private fun videoResponse(
        regionRestriction: RegionRestriction = RegionRestriction(),
        contentRating: ContentRating = ContentRating(),
    ) = VideoListResponse(
        items = listOf(
            VideoItem(
                contentDetails = ContentDetails(
                    regionRestriction = regionRestriction,
                    contentRating = contentRating,
                )
            )
        )
    )
}
