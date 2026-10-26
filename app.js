/**
 * Military Identification & Memorization App (Russia & China)
 * Material Design 3 Edition
 */

(function () {
  'use strict';

  // --- State Variables ---
  let weapons = typeof WEAPONS_DATA !== 'undefined' ? WEAPONS_DATA : [];
  let currentMode = 'flashcard'; // flashcard | quiz | gallery | stats
  let selectedCountry = 'all';    // all | russia | china
  let selectedCategory = 'all';   // all | aircraft | firearm | artillery | missile | bomb
  let onlyWeak = false;

  let filteredWeapons = [];
  let currentCardIndex = 0;
  let isCardFlipped = false;

  // Quiz state
  let quizCurrentWeapon = null;
  let quizOptions = [];
  let quizScore = 0;
  let quizAttempted = 0;
  let quizAnswered = false;

  // LocalStorage Key
  const STORAGE_KEY = 'military_id_progress_v2';
  let progressData = loadProgress();

  // --- DOM Elements ---
  const countryChipsContainer = document.getElementById('country-chips');
  const categoryChipsContainer = document.getElementById('category-chips');
  const navTabBtns = document.querySelectorAll('.nav-tab-btn');

  const flashcardView = document.getElementById('flashcard-view');
  const quizView = document.getElementById('quiz-view');
  const galleryView = document.getElementById('gallery-view');
  const statsView = document.getElementById('stats-view');

  const flashcardWrapper = document.getElementById('flashcard-wrapper');
  const cardIndexEl = document.getElementById('card-index');
  const cardTotalEl = document.getElementById('card-total');
  const currentFilterLabelEl = document.getElementById('current-filter-label');

  // Flashcard Front
  const cardFrontImg = document.getElementById('card-front-img');
  const frontCountryBadge = document.getElementById('front-country-badge');
  const frontCategoryBadge = document.getElementById('front-category-badge');

  // Flashcard Back
  const backWeaponName = document.getElementById('back-weapon-name');
  const backWeaponEn = document.getElementById('back-weapon-en');
  const backWeaponNato = document.getElementById('back-weapon-nato');
  const backWeaponAppearance = document.getElementById('back-weapon-appearance');
  const backWeaponFeatures = document.getElementById('back-weapon-features');
  const backWeaponSpecs = document.getElementById('back-weapon-specs');
  const backWeaponLicense = document.getElementById('back-weapon-license');
  const backWikiLink = document.getElementById('back-wiki-link');

  // Rating & Nav Buttons
  const btnRateAgain = document.getElementById('btn-rate-again');
  const btnRateGood = document.getElementById('btn-rate-good');
  const btnRateEasy = document.getElementById('btn-rate-easy');
  const btnPrevCard = document.getElementById('btn-prev-card');
  const btnNextCard = document.getElementById('btn-next-card');
  const btnShuffle = document.getElementById('btn-shuffle');
  const btnWeakOnly = document.getElementById('btn-weak-only');
  const btnResetData = document.getElementById('btn-reset-data');

  // Quiz Elements
  const quizCountryBadge = document.getElementById('quiz-country-badge');
  const quizCategoryBadge = document.getElementById('quiz-category-badge');
  const quizImage = document.getElementById('quiz-image');
  const quizOptionsContainer = document.getElementById('quiz-options');
  const quizFeedback = document.getElementById('quiz-feedback');
  const quizFeedbackTitle = document.getElementById('quiz-feedback-title');
  const quizFeedbackIcon = document.getElementById('quiz-feedback-icon');
  const quizFeedbackText = document.getElementById('quiz-feedback-text');
  const quizFeedbackAppearance = document.getElementById('quiz-feedback-appearance');
  const btnQuizNext = document.getElementById('btn-quiz-next');
  const quizScoreVal = document.getElementById('quiz-score-val');
  const quizAttemptedVal = document.getElementById('quiz-attempted-val');

  // Gallery Elements
  const galleryGrid = document.getElementById('gallery-grid');
  const gallerySearch = document.getElementById('gallery-search');

  // Modal Elements
  const modalBackdrop = document.getElementById('weapon-detail-modal');
  const modalCloseBtn = document.getElementById('modal-close-btn');
  const modalImg = document.getElementById('modal-img');
  const modalCountryBadge = document.getElementById('modal-country-badge');
  const modalCategoryBadge = document.getElementById('modal-category-badge');
  const modalName = document.getElementById('modal-name');
  const modalSub = document.getElementById('modal-sub');
  const modalNato = document.getElementById('modal-nato');
  const modalAppearance = document.getElementById('modal-appearance');
  const modalFeatures = document.getElementById('modal-features');
  const modalSpecs = document.getElementById('modal-specs');
  const modalLicense = document.getElementById('modal-license');
  const modalWikiLink = document.getElementById('modal-wiki-link');

  // --- LocalStorage Management ---
  function loadProgress() {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved ? JSON.parse(saved) : {};
    } catch (e) {
      console.warn('Could not load localStorage progress:', e);
      return {};
    }
  }

  function saveProgress() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(progressData));
    } catch (e) {
      console.warn('Could not save localStorage progress:', e);
    }
  }

  function getItemStatus(id) {
    return progressData[id]?.status || 'unseen';
  }

  function setItemStatus(id, newStatus) {
    if (!progressData[id]) {
      progressData[id] = { status: 'unseen', again: 0, good: 0, easy: 0, reviewed: 0 };
    }
    progressData[id].status = newStatus;
    if (newStatus === 'learning') progressData[id].again++;
    if (newStatus === 'reviewing') progressData[id].good++;
    if (newStatus === 'mastered') progressData[id].easy++;
    progressData[id].reviewed++;
    progressData[id].lastSeen = Date.now();
    saveProgress();
    updateBadgeCounts();
  }

  // --- Filtering Logic ---
  function applyFilters() {
    filteredWeapons = weapons.filter(w => {
      // Country Filter
      if (selectedCountry !== 'all' && w.country !== selectedCountry) return false;
      // Category Filter
      if (selectedCategory !== 'all' && w.category !== selectedCategory) return false;
      // Weak Only Filter
      if (onlyWeak) {
        const s = getItemStatus(w.id);
        if (s !== 'learning') return false;
      }
      return true;
    });

    currentCardIndex = 0;
    isCardFlipped = false;
    if (flashcardWrapper) flashcardWrapper.classList.remove('flipped');

    updateFilterLabels();
    updateBadgeCounts();

    if (currentMode === 'flashcard') renderFlashcard();
    else if (currentMode === 'quiz') startNextQuizQuestion();
    else if (currentMode === 'gallery') renderGallery();
    else if (currentMode === 'stats') renderStats();
  }

  function updateFilterLabels() {
    let countryTxt = selectedCountry === 'all' ? '🇷🇺 ロシア & 🇨🇳 中国' : (selectedCountry === 'russia' ? '🇷🇺 ロシア' : '🇨🇳 中国');
    let categoryMap = {
      'all': 'すべて',
      'aircraft': '戦闘機・航空機',
      'firearm': '火器・小火器',
      'artillery': '火砲・自走砲',
      'missile': 'ミサイル',
      'bomb': '爆弾'
    };
    let catTxt = categoryMap[selectedCategory] || 'すべて';
    if (onlyWeak) catTxt += ' [苦手特訓中]';
    currentFilterLabelEl.textContent = `${countryTxt} ・ ${catTxt}`;
  }

  function updateBadgeCounts() {
    const totalAll = weapons.length;
    const totalRu = weapons.filter(w => w.country === 'russia').length;
    const totalCn = weapons.filter(w => w.country === 'china').length;

    document.getElementById('count-country-all').textContent = totalAll;
    document.getElementById('count-country-ru').textContent = totalRu;
    document.getElementById('count-country-cn').textContent = totalCn;
  }

  // --- MODE 1: Flashcard Logic ---
  function renderFlashcard() {
    if (filteredWeapons.length === 0) {
      cardFrontImg.src = '';
      frontCountryBadge.textContent = 'なし';
      frontCategoryBadge.textContent = '対象データなし';
      cardIndexEl.textContent = '0';
      cardTotalEl.textContent = '0';
      backWeaponName.textContent = '該当する兵器がありません';
      backWeaponAppearance.textContent = 'フィルター条件を変更してください。';
      backWeaponFeatures.innerHTML = '';
      backWeaponSpecs.innerHTML = '';
      return;
    }

    if (currentCardIndex >= filteredWeapons.length) currentCardIndex = 0;
    if (currentCardIndex < 0) currentCardIndex = filteredWeapons.length - 1;

    const w = filteredWeapons[currentCardIndex];
    cardIndexEl.textContent = currentCardIndex + 1;
    cardTotalEl.textContent = filteredWeapons.length;

    // Reset Flip
    isCardFlipped = false;
    flashcardWrapper.classList.remove('flipped');

    // Front View
    cardFrontImg.src = w.image_url || '';
    cardFrontImg.onerror = function () {
      this.src = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300" viewBox="0 0 400 300"><rect width="400" height="300" fill="%23f1f5f9"/><text x="50%" y="50%" fill="%2364748b" font-size="16" font-family="sans-serif" text-anchor="middle" dominant-baseline="middle">画像読み込み中 / 未取得</text></svg>';
    };

    frontCountryBadge.textContent = w.country_label;
    frontCountryBadge.className = `badge-tag ${w.country}`;
    frontCategoryBadge.textContent = w.category_label;

    // Back View
    backWeaponName.textContent = w.name_ja;
    backWeaponEn.textContent = w.name_en;
    backWeaponNato.textContent = w.nato_or_alt || 'NATOコード / 呼称';
    backWeaponAppearance.textContent = w.appearance;

    // Features
    backWeaponFeatures.innerHTML = '';
    (w.features || []).forEach(feat => {
      const li = document.createElement('li');
      li.textContent = feat;
      backWeaponFeatures.appendChild(li);
    });

    // Specs
    backWeaponSpecs.innerHTML = '';
    if (w.specs) {
      for (const [key, val] of Object.entries(w.specs)) {
        const item = document.createElement('div');
        item.className = 'spec-item';
        item.innerHTML = `<span class="spec-key">${key}</span><span class="spec-val">${val}</span>`;
        backWeaponSpecs.appendChild(item);
      }
    }

    // License
    const lic = w.license || {};
    backWeaponLicense.textContent = `Photo: ${lic.author || 'Wikimedia Commons'} (${lic.license || 'CC BY-SA'})`;
    backWikiLink.href = lic.source_url || `https://en.wikipedia.org/wiki/${w.wiki_title || ''}`;
  }

  function flipFlashcard() {
    if (filteredWeapons.length === 0) return;
    isCardFlipped = !isCardFlipped;
    flashcardWrapper.classList.toggle('flipped', isCardFlipped);
  }

  function rateCurrentCard(rating) {
    if (filteredWeapons.length === 0) return;
    const w = filteredWeapons[currentCardIndex];
    let status = 'learning';
    if (rating === 'again') {
      status = 'learning';
      // 学習中のアイテムは数問後に再出題されるように近くに再挿入
      if (filteredWeapons.length > 3) {
        filteredWeapons.splice(currentCardIndex + 3, 0, w);
      }
    } else if (rating === 'good') {
      status = 'reviewing';
    } else if (rating === 'easy') {
      status = 'mastered';
    }

    setItemStatus(w.id, status);

    // 次のカードへ
    currentCardIndex = (currentCardIndex + 1) % filteredWeapons.length;
    renderFlashcard();
  }

  // --- MODE 2: Quiz Logic ---
  function startNextQuizQuestion() {
    if (filteredWeapons.length < 4) {
      // フィルター条件で選択肢が足りない場合は全体から補完
      if (weapons.length < 4) return;
    }

    quizAnswered = false;
    quizFeedback.classList.remove('show');

    // 1問をランダム選出
    const sourceList = filteredWeapons.length >= 4 ? filteredWeapons : weapons;
    const correctIdx = Math.floor(Math.random() * sourceList.length);
    quizCurrentWeapon = sourceList[correctIdx];

    // ダミー3問を選出（同カテゴリまたは同国を優先）
    let candidates = weapons.filter(w => w.id !== quizCurrentWeapon.id);
    let sameCatCandidates = candidates.filter(w => w.category === quizCurrentWeapon.category);
    let pool = sameCatCandidates.length >= 3 ? sameCatCandidates : candidates;
    
    // シャッフルして3つ選ぶ
    pool = [...pool].sort(() => 0.5 - Math.random());
    const dummies = pool.slice(0, 3);

    quizOptions = [quizCurrentWeapon, ...dummies].sort(() => 0.5 - Math.random());

    // 設問UI反映
    quizCountryBadge.textContent = quizCurrentWeapon.country_label;
    quizCountryBadge.className = `badge-tag ${quizCurrentWeapon.country}`;
    quizCategoryBadge.textContent = quizCurrentWeapon.category_label;
    quizImage.src = quizCurrentWeapon.image_url || '';

    // 選択肢ボタン生成
    quizOptionsContainer.innerHTML = '';
    quizOptions.forEach((opt, idx) => {
      const btn = document.createElement('button');
      btn.className = 'quiz-option-btn';
      btn.innerHTML = `
        <span>${opt.name_ja}</span>
        <span class="opt-sub">${opt.nato_or_alt || opt.name_en}</span>
      `;
      btn.addEventListener('click', () => handleQuizAnswer(opt, btn));
      quizOptionsContainer.appendChild(btn);
    });
  }

  function handleQuizAnswer(chosenOpt, clickedBtn) {
    if (quizAnswered) return;
    quizAnswered = true;
    quizAttempted++;

    const isCorrect = chosenOpt.id === quizCurrentWeapon.id;
    if (isCorrect) {
      quizScore++;
      clickedBtn.classList.add('correct');
      setItemStatus(quizCurrentWeapon.id, 'mastered');
      
      quizFeedback.className = 'quiz-feedback-box is-correct show';
      quizFeedbackIcon.textContent = 'check_circle';
      quizFeedbackIcon.style.color = '#10b981';
      quizFeedbackText.textContent = '正解！素晴らしい！';
    } else {
      clickedBtn.classList.add('wrong');
      setItemStatus(quizCurrentWeapon.id, 'learning');

      // 正解ボタンをハイライト
      const allBtns = quizOptionsContainer.querySelectorAll('.quiz-option-btn');
      allBtns.forEach((btn, idx) => {
        if (quizOptions[idx].id === quizCurrentWeapon.id) {
          btn.classList.add('correct');
        }
      });

      quizFeedback.className = 'quiz-feedback-box is-wrong show';
      quizFeedbackIcon.textContent = 'cancel';
      quizFeedbackIcon.style.color = '#f43f5e';
      quizFeedbackText.textContent = `惜しい！ 正解は【${quizCurrentWeapon.name_ja}】`;
    }

    quizScoreVal.textContent = quizScore;
    quizAttemptedVal.textContent = quizAttempted;

    quizFeedbackAppearance.innerHTML = `
      <strong>見た目の識別ポイント:</strong> ${quizCurrentWeapon.appearance}<br>
      <span style="font-size:0.8rem; color:var(--md-sys-color-on-surface-variant);">${quizCurrentWeapon.features?.[0] || ''}</span>
    `;

    // 選択肢ボタンを無効化
    const allBtns = quizOptionsContainer.querySelectorAll('.quiz-option-btn');
    allBtns.forEach(btn => btn.disabled = true);
  }

  // --- MODE 3: Gallery Logic ---
  function renderGallery() {
    galleryGrid.innerHTML = '';
    const query = (gallerySearch.value || '').trim().toLowerCase();

    const items = filteredWeapons.filter(w => {
      if (!query) return true;
      const matchName = (w.name_ja + ' ' + w.name_en + ' ' + (w.nato_or_alt || '')).toLowerCase().includes(query);
      const matchApp = (w.appearance || '').toLowerCase().includes(query);
      const matchFeat = (w.features || []).join(' ').toLowerCase().includes(query);
      return matchName || matchApp || matchFeat;
    });

    if (items.length === 0) {
      galleryGrid.innerHTML = '<div style="grid-column: 1/-1; text-align:center; padding:3rem; color:var(--md-sys-color-on-surface-variant);">該当する兵器が見つかりませんでした。</div>';
      return;
    }

    items.forEach(w => {
      const card = document.createElement('div');
      card.className = 'gallery-card';
      const status = getItemStatus(w.id);

      card.innerHTML = `
        <div class="gallery-card-img">
          <div class="gallery-status-indicator ${status}" title="学習状態: ${status}"></div>
          <div class="card-badges" style="top:8px; left:8px;">
            <span class="badge-tag ${w.country}" style="padding:2px 6px; font-size:0.7rem;">${w.country_label}</span>
            <span class="badge-tag category" style="padding:2px 6px; font-size:0.7rem;">${w.category_label}</span>
          </div>
          <img src="${w.image_url || ''}" alt="${w.name_ja}" loading="lazy">
        </div>
        <div class="gallery-card-body">
          <div class="gallery-card-title">${w.name_ja}</div>
          <div class="gallery-card-sub">
            <span>${w.nato_or_alt || w.name_en}</span>
          </div>
          <div class="gallery-card-desc">
            ${w.appearance}
          </div>
        </div>
      `;

      card.addEventListener('click', () => openModal(w));
      galleryGrid.appendChild(card);
    });
  }

  // --- MODE 4: Stats Logic ---
  function renderStats() {
    let masteredCount = 0;
    let reviewingCount = 0;
    let learningCount = 0;
    let unseenCount = 0;

    weapons.forEach(w => {
      const s = getItemStatus(w.id);
      if (s === 'mastered') masteredCount++;
      else if (s === 'reviewing') reviewingCount++;
      else if (s === 'learning') learningCount++;
      else unseenCount++;
    });

    const total = weapons.length || 1;
    const overallPct = Math.round(((masteredCount + reviewingCount * 0.5) / total) * 100);

    document.getElementById('stat-mastered-count').textContent = masteredCount;
    document.getElementById('stat-learning-count').textContent = reviewingCount;
    document.getElementById('stat-weak-count').textContent = learningCount;
    document.getElementById('stat-overall-percent').textContent = overallPct + '%';

    // Breakdown by Country & Category
    const breakdownContainer = document.getElementById('stats-breakdown-container');
    breakdownContainer.innerHTML = '';

    const groups = [
      { label: '🇷🇺 ロシア 全体', filter: w => w.country === 'russia' },
      { label: '🇨🇳 中国 全体', filter: w => w.country === 'china' },
      { label: '✈️ 戦闘機・航空機 (RU/CN)', filter: w => w.category === 'aircraft' },
      { label: '🔫 火器・小火器 (RU/CN)', filter: w => w.category === 'firearm' },
      { label: '💥 火砲・自走砲 (RU/CN)', filter: w => w.category === 'artillery' },
      { label: '🚀 ミサイル (RU/CN)', filter: w => w.category === 'missile' },
      { label: '💣 爆弾 (RU/CN)', filter: w => w.category === 'bomb' }
    ];

    groups.forEach(g => {
      const subset = weapons.filter(g.filter);
      const subTotal = subset.length;
      if (subTotal === 0) return;

      const subMastered = subset.filter(w => getItemStatus(w.id) === 'mastered').length;
      const subPct = Math.round((subMastered / subTotal) * 100);

      const row = document.createElement('div');
      row.className = 'breakdown-row';
      row.innerHTML = `
        <div class="breakdown-row-header">
          <span>${g.label}</span>
          <span style="font-family:var(--font-mono);">${subMastered} / ${subTotal} (${subPct}%)</span>
        </div>
        <div class="m3-progress-track">
          <div class="m3-progress-bar" style="width: ${subPct}%;"></div>
        </div>
      `;
      breakdownContainer.appendChild(row);
    });
  }

  // --- Modal Detail Popup ---
  function openModal(w) {
    modalImg.src = w.image_url || '';
    modalCountryBadge.textContent = w.country_label;
    modalCountryBadge.className = `badge-tag ${w.country}`;
    modalCategoryBadge.textContent = w.category_label;

    modalName.textContent = w.name_ja;
    modalSub.textContent = `${w.name_en} ・ `;
    modalNato.textContent = w.nato_or_alt || 'NATOコード / 呼称';

    modalAppearance.textContent = w.appearance;

    modalFeatures.innerHTML = '';
    (w.features || []).forEach(f => {
      const li = document.createElement('li');
      li.textContent = f;
      modalFeatures.appendChild(li);
    });

    modalSpecs.innerHTML = '';
    if (w.specs) {
      for (const [k, v] of Object.entries(w.specs)) {
        const item = document.createElement('div');
        item.className = 'spec-item';
        item.innerHTML = `<span class="spec-key">${k}</span><span class="spec-val">${v}</span>`;
        modalSpecs.appendChild(item);
      }
    }

    const lic = w.license || {};
    modalLicense.textContent = `Photo: ${lic.author || 'Wikimedia Commons'} (${lic.license || 'CC BY-SA'})`;
    modalWikiLink.href = lic.source_url || `https://en.wikipedia.org/wiki/${w.wiki_title || ''}`;

    modalBackdrop.classList.add('open');
  }

  function closeModal() {
    modalBackdrop.classList.remove('open');
  }

  // --- Event Listeners ---

  // Navigation Tabs
  navTabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      navTabBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentMode = btn.dataset.mode;

      flashcardView.style.display = currentMode === 'flashcard' ? 'flex' : 'none';
      quizView.style.display = currentMode === 'quiz' ? 'flex' : 'none';
      galleryView.style.display = currentMode === 'gallery' ? 'flex' : 'none';
      statsView.style.display = currentMode === 'stats' ? 'flex' : 'none';

      applyFilters();
    });
  });

  // Country Chips
  countryChipsContainer.addEventListener('click', e => {
    const chip = e.target.closest('.m3-chip');
    if (!chip) return;
    countryChipsContainer.querySelectorAll('.m3-chip').forEach(c => c.classList.remove('active'));
    chip.classList.add('active');
    selectedCountry = chip.dataset.country;
    applyFilters();
  });

  // Category Chips
  categoryChipsContainer.addEventListener('click', e => {
    const chip = e.target.closest('.m3-chip');
    if (!chip) return;
    categoryChipsContainer.querySelectorAll('.m3-chip').forEach(c => c.classList.remove('active'));
    chip.classList.add('active');
    selectedCategory = chip.dataset.category;
    applyFilters();
  });

  // Flashcard Controls
  flashcardWrapper.addEventListener('click', flipFlashcard);

  btnRateAgain.addEventListener('click', e => { e.stopPropagation(); rateCurrentCard('again'); });
  btnRateGood.addEventListener('click', e => { e.stopPropagation(); rateCurrentCard('good'); });
  btnRateEasy.addEventListener('click', e => { e.stopPropagation(); rateCurrentCard('easy'); });

  btnPrevCard.addEventListener('click', () => {
    if (filteredWeapons.length === 0) return;
    currentCardIndex = (currentCardIndex - 1 + filteredWeapons.length) % filteredWeapons.length;
    renderFlashcard();
  });

  btnNextCard.addEventListener('click', () => {
    if (filteredWeapons.length === 0) return;
    currentCardIndex = (currentCardIndex + 1) % filteredWeapons.length;
    renderFlashcard();
  });

  // Header Actions
  btnShuffle.addEventListener('click', () => {
    filteredWeapons.sort(() => 0.5 - Math.random());
    currentCardIndex = 0;
    renderFlashcard();
  });

  btnWeakOnly.addEventListener('click', () => {
    onlyWeak = !onlyWeak;
    btnWeakOnly.style.backgroundColor = onlyWeak ? '#f43f5e' : '';
    btnWeakOnly.style.color = onlyWeak ? '#ffffff' : '';
    applyFilters();
  });

  btnResetData.addEventListener('click', () => {
    if (confirm('学習進捗データをすべてリセットしますか？')) {
      progressData = {};
      saveProgress();
      applyFilters();
    }
  });

  // Quiz Next
  btnQuizNext.addEventListener('click', startNextQuizQuestion);

  // Gallery Search
  gallerySearch.addEventListener('input', renderGallery);

  // Modal Close
  modalCloseBtn.addEventListener('click', closeModal);
  modalBackdrop.addEventListener('click', e => {
    if (e.target === modalBackdrop) closeModal();
  });

  // Mobile Modal Elements
  const btnMobileConnect = document.getElementById('btn-mobile-connect');
  const mobileModal = document.getElementById('mobile-connect-modal');
  const mobileModalCloseBtn = document.getElementById('mobile-modal-close-btn');
  const mobileQrImg = document.getElementById('mobile-qr-img');
  const mobileUrlText = document.getElementById('mobile-url-text');
  const btnApkDownloadLink = document.getElementById('btn-apk-download-link');
  const btnQrShowWeb = document.getElementById('btn-qr-show-web');
  const btnQrShowApk = document.getElementById('btn-qr-show-apk');

  let currentTargetWebUrl = '';
  let currentTargetApkUrl = '';

  function openMobileModal() {
    let host = window.location.hostname;
    let port = window.location.port || '8080';
    // localhostや127.0.0.1の場合はPCのWi-FiローカルIPアドレスを使用
    if (host === 'localhost' || host === '127.0.0.1' || !host) {
      host = '192.168.0.192';
    }
    currentTargetWebUrl = `http://${host}:${port}/index.html`;
    currentTargetApkUrl = `http://${host}:${port}/military-id-app.apk`;
    
    mobileUrlText.textContent = currentTargetWebUrl;
    if (btnApkDownloadLink) btnApkDownloadLink.href = currentTargetApkUrl;
    
    // デフォルトはWeb版QR
    setQrUrl(currentTargetWebUrl);
    mobileModal.classList.add('open');
  }

  function setQrUrl(url) {
    mobileQrImg.src = `https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${encodeURIComponent(url)}`;
  }

  if (btnQrShowWeb) {
    btnQrShowWeb.addEventListener('click', () => {
      setQrUrl(currentTargetWebUrl);
      btnQrShowWeb.style.borderColor = '#38bdf8';
      if (btnQrShowApk) btnQrShowApk.style.borderColor = '';
    });
  }

  if (btnQrShowApk) {
    btnQrShowApk.addEventListener('click', () => {
      setQrUrl(currentTargetApkUrl);
      btnQrShowApk.style.borderColor = '#10b981';
      if (btnQrShowWeb) btnQrShowWeb.style.borderColor = '';
    });
  }

  function closeMobileModal() {
    mobileModal.classList.remove('open');
  }

  if (btnMobileConnect) btnMobileConnect.addEventListener('click', openMobileModal);
  if (mobileModalCloseBtn) mobileModalCloseBtn.addEventListener('click', closeMobileModal);
  if (mobileModal) {
    mobileModal.addEventListener('click', e => {
      if (e.target === mobileModal) closeMobileModal();
    });
  }

  // Keyboard Shortcuts
  window.addEventListener('keydown', e => {
    if (mobileModal && mobileModal.classList.contains('open')) {
      if (e.key === 'Escape') closeMobileModal();
      return;
    }
    if (modalBackdrop.classList.contains('open')) {
      if (e.key === 'Escape') closeModal();
      return;
    }

    if (currentMode === 'flashcard') {
      if (e.code === 'Space' || e.code === 'Enter') {
        e.preventDefault();
        flipFlashcard();
      } else if (e.key === '1') {
        rateCurrentCard('again');
      } else if (e.key === '2') {
        rateCurrentCard('good');
      } else if (e.key === '3') {
        rateCurrentCard('easy');
      } else if (e.key === 'ArrowLeft') {
        btnPrevCard.click();
      } else if (e.key === 'ArrowRight') {
        btnNextCard.click();
      } else if (e.key.toLowerCase() === 'r') {
        btnShuffle.click();
      }
    }
  });

  // Touch Swipe for Mobile
  let touchStartX = 0;
  let touchEndX = 0;
  flashcardWrapper.addEventListener('touchstart', e => {
    touchStartX = e.changedTouches[0].screenX;
  }, { passive: true });

  flashcardWrapper.addEventListener('touchend', e => {
    touchEndX = e.changedTouches[0].screenX;
    const diff = touchEndX - touchStartX;
    if (Math.abs(diff) > 60) {
      if (diff > 0) btnPrevCard.click();
      else btnNextCard.click();
    }
  }, { passive: true });

  // --- Initial Start ---
  applyFilters();

})();
