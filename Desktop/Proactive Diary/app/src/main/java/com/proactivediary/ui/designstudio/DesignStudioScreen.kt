package com.proactivediary.ui.designstudio

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.designstudio.components.DesignNavBar
import com.proactivediary.ui.designstudio.components.DesignSummaryCard
import com.proactivediary.ui.designstudio.components.SectionDivider
import com.proactivediary.ui.designstudio.components.StickyFooter
import com.proactivediary.ui.designstudio.sections.CanvasSection
import com.proactivediary.ui.designstudio.sections.DetailsSection
import com.proactivediary.ui.designstudio.sections.FormSection
import com.proactivediary.ui.designstudio.sections.MarkSection
import com.proactivediary.ui.designstudio.sections.SoulSection
import com.proactivediary.ui.designstudio.sections.TouchSection
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun DesignStudioScreen(
    onNavigateToGoals: () -> Unit,
    isEditMode: Boolean = false,
    viewModel: DesignStudioViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    // Track which section indices have been revealed
    val revealedSections = remember { mutableStateMapOf<Int, Boolean>() }

    // Detect visibility for scroll-reveal animations
    // Items: 0=preview, 1=divider, 2=soul(section0), 3=form(section1), 4=touch(section2),
    //        5=canvas(section3), 6=details(section4), 7=mark(section5), 8=summary, 9=bottomPadding
    val sectionIndices = listOf(2, 3, 4, 5, 6, 7, 8) // indices of animated items

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.map { it.index }
        }.collect { visibleIndices ->
            sectionIndices.forEach { index ->
                if (index in visibleIndices && revealedSections[index] != true) {
                    revealedSections[index] = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DiaryColors.Paper)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DesignNavBar()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 0: Diary Preview
                    item {
                        DiaryPreview(state = state)
                    }

                    // 1: First divider
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionDivider()
                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    // 2: Soul — Section 01
                    item {
                        ScrollRevealWrapper(
                            index = 2,
                            revealed = revealedSections[2] == true,
                            staggerOffset = 0
                        ) {
                            SoulSection(
                                selectedColor = state.selectedColor,
                                onColorSelected = viewModel::selectColor
                            )
                        }
                    }

                    // 3: Form — Section 02
                    item {
                        ScrollRevealWrapper(
                            index = 3,
                            revealed = revealedSections[3] == true,
                            staggerOffset = 1
                        ) {
                            FormSection(
                                selectedForm = state.selectedForm,
                                onFormSelected = viewModel::selectForm
                            )
                        }
                    }

                    // 4: Touch — Section 03
                    item {
                        ScrollRevealWrapper(
                            index = 4,
                            revealed = revealedSections[4] == true,
                            staggerOffset = 2
                        ) {
                            TouchSection(
                                selectedTexture = state.selectedTexture,
                                onTextureSelected = viewModel::selectTexture
                            )
                        }
                    }

                    // 5: Canvas — Section 04
                    item {
                        ScrollRevealWrapper(
                            index = 5,
                            revealed = revealedSections[5] == true,
                            staggerOffset = 3
                        ) {
                            CanvasSection(
                                selectedCanvas = state.selectedCanvas,
                                onCanvasSelected = viewModel::selectCanvas
                            )
                        }
                    }

                    // 6: Details — Section 05
                    item {
                        ScrollRevealWrapper(
                            index = 6,
                            revealed = revealedSections[6] == true,
                            staggerOffset = 4
                        ) {
                            DetailsSection(
                                features = state.features,
                                onToggleFeature = viewModel::toggleFeature
                            )
                        }
                    }

                    // 7: Mark — Section 06
                    item {
                        ScrollRevealWrapper(
                            index = 7,
                            revealed = revealedSections[7] == true,
                            staggerOffset = 5
                        ) {
                            MarkSection(
                                markText = state.markText,
                                markPosition = state.markPosition,
                                markFont = state.markFont,
                                onTextChanged = viewModel::updateMarkText,
                                onPositionSelected = viewModel::selectMarkPosition,
                                onFontSelected = viewModel::selectMarkFont
                            )
                        }
                    }

                    // 8: Summary Card
                    item {
                        ScrollRevealWrapper(
                            index = 8,
                            revealed = revealedSections[8] == true,
                            staggerOffset = 6
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))
                            DesignSummaryCard(state = state)
                        }
                    }

                    // 9: Bottom padding for footer clearance
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Sticky footer pinned to bottom
        StickyFooter(
            isEditMode = isEditMode,
            isSaving = state.isSaving,
            onTap = {
                viewModel.saveAndNavigate { onNavigateToGoals() }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ScrollRevealWrapper(
    index: Int,
    revealed: Boolean,
    staggerOffset: Int,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = staggerOffset * 150
        ),
        label = "revealAlpha_$index"
    )

    val translationY by animateFloatAsState(
        targetValue = if (revealed) 0f else 30f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = staggerOffset * 150
        ),
        label = "revealTranslateY_$index"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translationY * density
            }
    ) {
        content()
    }
}
