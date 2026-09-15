package ais.tee.web

import android.webkit.WebView
import ais.tee.data.model.WebAiService

internal data class ProviderWebTweak(
    val id: String,
    val css: String,
    val script: String = ""
)

internal object ProviderWebTweakRegistry {
    private const val CLAUDE_OVERLAY_SELECTOR =
        "[role=\"dialog\"],[role=\"menu\"],[role=\"tooltip\"],[role=\"listbox\"]," +
            "[data-radix-popper-content-wrapper]"

    private val mobileBaseline = ProviderWebTweak(
        id = "mobile-baseline",
        css = """
            html {
                scroll-behavior: auto !important;
            }
            @media (prefers-reduced-motion: reduce) {
                *, *::before, *::after {
                    animation-duration: 0.01ms !important;
                    animation-iteration-count: 1 !important;
                    transition-duration: 0.01ms !important;
                }
            }
        """.trimIndent()
    )

    // Mobile WebView subset of the proven Claude-Smooth userscript. Keep it static and
    // provider-scoped: no remote script loading, credential access, or page-text collection.
    private val claudeSmooth = ProviderWebTweak(
        id = "claude-smooth",
        css = """
            *, *::before, *::after {
                animation-duration: 0.001s !important;
                animation-iteration-count: 1 !important;
                transition-duration: 0.001s !important;
                scroll-behavior: auto !important;
            }
            [class*="animate-spin"], [class*="animate-pulse"] {
                animation-duration: 1s !important;
                animation-iteration-count: infinite !important;
            }
            [class*="blur"], [class*="backdrop"] {
                backdrop-filter: none !important;
                -webkit-backdrop-filter: none !important;
            }
            [class*="shadow"] {
                box-shadow: none !important;
            }
            $CLAUDE_OVERLAY_SELECTOR {
                box-shadow: 0 4px 16px rgba(0, 0, 0, 0.28) !important;
            }
            *:not($CLAUDE_OVERLAY_SELECTOR) {
                will-change: auto !important;
            }
            .llmbench-cv-turn {
                content-visibility: auto;
                contain-intrinsic-size: auto 600px;
            }
            .llmbench-cv-code {
                content-visibility: auto;
                contain-intrinsic-size: auto 300px;
            }
            .llmbench-cv-side {
                content-visibility: auto;
                contain-intrinsic-size: auto 44px;
            }
        """.trimIndent(),
        script = """
            (() => {
                const STATE_KEY = '__llmbenchClaudeSmoothV1';
                const existing = window[STATE_KEY];
                if (existing) {
                    existing.tag();
                    return;
                }

                const OVERLAY_SELECTOR = '$CLAUDE_OVERLAY_SELECTOR';
                const TURN_SELECTOR = '[data-testid^="conversation-turn"],[data-test-render-count],' +
                    'div.font-claude-message,div.font-claude-response,[data-testid="user-message"]';
                const SIDE_SELECTOR = 'nav a[href^="/chat/"],nav li,aside a[href^="/chat/"]';
                const TAIL = 2;
                const isDesktop = () => window.matchMedia('(pointer: fine)').matches && window.innerWidth >= 900;

                const tagTurns = () => {
                    const turns = Array.from(document.querySelectorAll(TURN_SELECTOR));
                    turns.forEach((turn, index) => {
                        turn.classList.toggle('llmbench-cv-turn', index < turns.length - TAIL);
                    });
                };

                const tagSide = () => {
                    const compactSide = !isDesktop();
                    document.querySelectorAll(SIDE_SELECTOR).forEach((item) => {
                        item.classList.toggle('llmbench-cv-side', compactSide);
                    });
                };

                const tag = () => {
                    tagTurns();
                    tagSide();
                    document.querySelectorAll('pre').forEach((code) => {
                        if (!code.closest(OVERLAY_SELECTOR)) code.classList.add('llmbench-cv-code');
                    });
                    document.querySelectorAll('img:not([data-llmbench-lazy])').forEach((image) => {
                        image.loading = 'lazy';
                        image.decoding = 'async';
                        image.dataset.llmbenchLazy = '1';
                    });
                };

                const idle = window.requestIdleCallback
                    ? (callback) => window.requestIdleCallback(callback, { timeout: 500 })
                    : (callback) => window.setTimeout(callback, 100);
                let turnsQueued = false;
                const scheduleTurns = () => {
                    if (turnsQueued) return;
                    turnsQueued = true;
                    idle(() => {
                        turnsQueued = false;
                        tagTurns();
                    });
                };
                let sideQueued = false;
                const scheduleSide = () => {
                    if (sideQueued) return;
                    sideQueued = true;
                    idle(() => {
                        sideQueued = false;
                        tagSide();
                    });
                };

                const processNode = (node) => {
                    if (!(node instanceof Element)) return;
                    const candidates = [node, ...node.querySelectorAll('pre, img, nav a[href^="/chat/"], nav li, aside a[href^="/chat/"], .llmbench-cv-side')];
                    candidates.forEach((candidate) => {
                        if (candidate.matches?.('pre') && !candidate.closest(OVERLAY_SELECTOR)) candidate.classList.add('llmbench-cv-code');
                        if (candidate.matches?.('img')) {
                            candidate.loading = 'lazy';
                            candidate.decoding = 'async';
                            candidate.dataset.llmbenchLazy = '1';
                        }
                        const isSide = candidate.matches?.(SIDE_SELECTOR) === true;
                        if (isSide || candidate.classList.contains('llmbench-cv-side')) {
                            candidate.classList.toggle('llmbench-cv-side', isSide && !isDesktop());
                        }
                    });
                };

                const observer = new MutationObserver((mutations) => {
                    let turnBoundaryMayHaveChanged = false;
                    mutations.forEach((mutation) => {
                        mutation.addedNodes.forEach((node) => {
                            processNode(node);
                            if (node instanceof Element && (node.matches?.(TURN_SELECTOR) || node.querySelector?.(TURN_SELECTOR))) {
                                turnBoundaryMayHaveChanged = true;
                            }
                        });
                        mutation.removedNodes.forEach((node) => {
                            if (node instanceof Element && (node.matches?.(TURN_SELECTOR) || node.querySelector?.(TURN_SELECTOR))) {
                                turnBoundaryMayHaveChanged = true;
                            }
                        });
                    });
                    if (turnBoundaryMayHaveChanged) scheduleTurns();
                });
                const start = () => {
                    tag();
                    observer.observe(document.body, { childList: true, subtree: true });
                    window.addEventListener('resize', scheduleSide, { passive: true });
                };
                window[STATE_KEY] = { tag, observer };

                if (document.body) {
                    start();
                } else {
                    const boot = new MutationObserver(() => {
                        if (!document.body) return;
                        boot.disconnect();
                        start();
                    });
                    boot.observe(document.documentElement, { childList: true });
                }
            })();
        """.trimIndent()
    )

    private val providerTweaks = WebAiService.entries.associateWith { service ->
        when (service) {
            WebAiService.CLAUDE -> listOf(mobileBaseline, claudeSmooth)
            else -> listOf(mobileBaseline)
        }
    }

    fun forProvider(service: WebAiService): List<ProviderWebTweak> =
        providerTweaks[service].orEmpty()
}

internal fun applyProviderWebTweaks(
    webView: WebView,
    service: WebAiService,
    pageUrl: String
) {
    if (!providerUrlMatches(service, pageUrl)) return

    val providerId = javascriptStringLiteral(service.id)
    ProviderWebTweakRegistry.forProvider(service).forEach { tweak ->
        val styleId = javascriptStringLiteral("llmbench-${service.id}-${tweak.id}")
        val css = javascriptStringLiteral(tweak.css)
        val script = """
            (() => {
                ${providerRuntimeGuardScript(service, "undefined")}

                const root = document.documentElement;
                if (!root) return;
                root.dataset.llmbenchProvider = $providerId;
                let style = document.getElementById($styleId);
                if (!style) {
                    style = document.createElement('style');
                    style.id = $styleId;
                    style.dataset.llmbench = 'provider-tweak';
                    (document.head || root).appendChild(style);
                }
                style.textContent = $css;
                ${tweak.script}
            })();
        """.trimIndent()
        webView.evaluateJavascript(script, null)
    }
}
