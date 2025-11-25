import React, { useState, useEffect } from 'react';
import { 
  Play, RotateCcw, Menu, Power, Cpu, Zap, Skull, ShieldAlert,
  ChevronLeft, ChevronRight, Disc, Radio, Activity, Hexagon, Trophy, Monitor
} from 'lucide-react';

// --- Assets & Constants ---

const COLORS = {
  cyan: '#00F0FF',
  pink: '#FF0099',
  purple: '#BD00FF',
  dark: '#050510',
  metal: '#1e1e2e',
};

// Character Data
const CHARACTERS = [
  { id: 'kairo', name: 'KAIRO', role: 'NETRUNNER', color: COLORS.cyan, icon: Cpu, desc: 'Prioritizes precision.' },
  { id: 'nova', name: 'NOVA', role: 'POP IDOL', color: COLORS.pink, icon: Zap, desc: 'Unpredictable and explosive.' },
  { id: 'titan', name: 'TITAN', role: 'ENFORCER', color: '#FF3300', icon: ShieldAlert, desc: 'Brute force and stability.' },
  { id: 'viper', name: 'VIPER', role: 'ASSASSIN', color: COLORS.purple, icon: Skull, desc: 'Silent protocol specialist.' }
];

// Chip Skin Data
const CHIP_SKINS = [
  { id: 'plasma', name: 'PLASMA', icon: Disc, render: (color) => <div className="w-1/2 h-1/2 rounded-full border-4 border-current opacity-50" style={{ color }} /> },
  { id: 'bio', name: 'BIOHAZARD', icon: Radio, render: (color) => <Radio className="w-3/5 h-3/5 animate-spin-slow" style={{ color }} /> },
  { id: 'tech', name: 'CIRCUIT', icon: Activity, render: (color) => <Activity className="w-3/5 h-3/5" style={{ color }} /> },
  { id: 'hex', name: 'HIVE', icon: Hexagon, render: (color) => <Hexagon className="w-3/5 h-3/5 fill-current opacity-40" style={{ color }} /> }
];

// Game Modes Data
const GAME_MODES = [
  { id: 'classic', name: 'NEON CLASSIC', desc: 'Standard 4-link protocol.' },
  { id: 'virus', name: 'VIRUS MODE', desc: 'Random blocks spawn every 3 turns.' },
];

// --- Sub-Components ---

const Background = ({ offset }) => (
  <div className="absolute inset-0 z-0 overflow-hidden pointer-events-none perspective-3d">
     {/* Ceiling Grid */}
     <div 
       className="absolute top-0 left-[-50%] right-[-50%] h-[50vh] origin-top"
       style={{ 
         background: `
           linear-gradient(transparent 50%, ${COLORS.purple} 100%), 
           repeating-linear-gradient(90deg, transparent 0, transparent 48px, ${COLORS.purple}40 50px),
           repeating-linear-gradient(180deg, transparent 0, transparent 48px, ${COLORS.purple}20 50px)
         `,
         transform: 'rotateX(-60deg) scale(2)',
         backgroundPosition: `center ${offset}px`
       }}
     />
     {/* Floor Grid */}
     <div 
       className="absolute bottom-0 left-[-50%] right-[-50%] h-[50vh] origin-bottom"
       style={{ 
         background: `
           linear-gradient(${COLORS.cyan} 0%, ${COLORS.dark} 80%), 
           repeating-linear-gradient(90deg, transparent 0, transparent 48px, ${COLORS.cyan}20 50px),
           repeating-linear-gradient(0deg, transparent 0, transparent 48px, ${COLORS.cyan}40 50px)
         `,
         transform: 'rotateX(60deg) scale(2)',
         backgroundPosition: `center ${-offset}px`
       }}
     />
     {/* Vignette */}
     <div className="absolute inset-0 bg-radial-gradient from-transparent via-black/40 to-black/90"></div>
  </div>
);

const ChipRenderer = ({ skin, color }) => {
  // Finds the render logic based on the selected skin ID
  const skinData = CHIP_SKINS.find(s => s.id === skin);
  if (!skinData) return null;
  
  return (
    <div className={`w-full h-full flex items-center justify-center opacity-80`} style={{ color: color, filter: `drop-shadow(0 0 2px ${color})` }}>
      {skinData.render(color)}
    </div>
  );
};

const ArcadeButton = ({ icon: Icon, color, onClick, label, size = 'md' }) => {
  const sizeClass = size === 'lg' ? 'w-20 h-20 md:w-24 md:h-24' : 'w-14 h-14 md:w-16 md:h-16';
  const glowColor = color === 'yellow' ? '#facc15' : color === 'red' ? '#ef4444' : color === 'green' ? '#10b981' : COLORS.purple;
  
  return (
    <div className="flex flex-col items-center gap-2 group relative z-50">
      <button 
        onClick={onClick}
        className={`${sizeClass} rounded-full relative flex items-center justify-center transition-transform active:scale-95 duration-75`}
        style={{
          background: `radial-gradient(circle at 30% 30%, ${glowColor}, #333)`,
          boxShadow: `
            0 10px 0 #1a1a1a, 
            0 10px 10px rgba(0,0,0,0.5),
            inset 0 -5px 10px rgba(0,0,0,0.5)
          `,
          border: '4px solid #222'
        }}
      >
        <div className="absolute inset-0 rounded-full opacity-30 pointer-events-none" 
             style={{ background: `radial-gradient(circle at 30% 30%, ${glowColor}, transparent 30%)`, boxShadow: `0 0 15px ${glowColor}` }} />
        <Icon className="text-white/90 drop-shadow-lg" size={size === 'lg' ? 40 : 28} />
      </button>
      <span className="text-[10px] md:text-xs font-mono font-bold tracking-widest text-slate-400 bg-black/50 px-2 rounded backdrop-blur-sm border border-slate-700 uppercase">
        {label}
      </span>
    </div>
  );
};

const PlayerDisplay = ({ player, char, score, active }) => {
  const color = char.color;
  const Icon = char.icon; // Destructure the Icon component

  return (
    <div className={`
      relative w-32 md:w-48 h-24 md:h-32 bg-black border-2 rounded-lg overflow-hidden flex flex-col
      transition-all duration-300
      ${active ? 'opacity-100 shadow-[0_0_20px_rgba(0,0,0,0.5)]' : 'opacity-40 grayscale'}
    `}
    style={{ borderColor: color, boxShadow: active ? `0 0 15px ${color}40` : 'none' }}
    >
      {/* Scanlines */}
      <div className="absolute inset-0 z-10 opacity-20 pointer-events-none bg-[url('https://media.giphy.com/media/xT0BQrD9bH5h4w8P5a/giphy.gif')] mix-blend-screen bg-cover"></div>
      
      <div className="flex-1 flex flex-col items-center justify-center p-2 relative z-0">
         <span className="text-xs md:text-sm font-mono text-slate-400 mb-1 flex items-center gap-2">
            {/* Ensure Icon is rendered correctly as JSX */}
            <Icon size={16} style={{ color }} /> {char.name}
         </span>
         <span className="text-4xl md:text-5xl font-black font-mono leading-none" style={{ color: color, textShadow: `0 0 10px ${color}` }}>
           {score}
         </span>
      </div>
      
      <div className="h-6 w-full flex items-center justify-center text-[10px] font-bold text-black" style={{ backgroundColor: color }}>
         {active ? '>> ACTIVE <<' : 'STANDBY'}
      </div>
    </div>
  );
};

// --- Game Logic and State ---

const useGameState = (p1Char, p2Char, p1Skin, p2Skin, gameMode, goToLobby) => {
  const ROWS = 6; 
  const COLS = 7;
  const [board, setBoard] = useState(Array(ROWS).fill(Array(COLS).fill(0)));
  const [currentPlayer, setCurrentPlayer] = useState(1);
  const [winner, setWinner] = useState(0);
  const [turnCount, setTurnCount] = useState(0);

  useEffect(() => {
    resetGame();
  }, [gameMode]);

  const resetGame = () => {
    setBoard(Array(ROWS).fill(null).map(() => Array(COLS).fill(0)));
    setCurrentPlayer(1);
    setWinner(0);
    setTurnCount(0);
  };

  const checkWin = (currentBoard, player) => {
    const R = ROWS, C = COLS;
    const checkLine = (r, c, dr, dc) => {
        let count = 0;
        for (let i = 0; i < 4; i++) {
            const nr = r + i * dr;
            const nc = c + i * dc;
            if (nr >= 0 && nr < R && nc >= 0 && nc < C && currentBoard[nr][nc] === player) {
                count++;
            } else {
                break;
            }
        }
        return count === 4;
    };

    for (let r = 0; r < R; r++) {
        for (let c = 0; c < C; c++) {
            if (currentBoard[r][c] === player) {
                // Horizontal (0, 1), Vertical (1, 0), Diagonals (1, 1), (1, -1)
                if (checkLine(r, c, 0, 1) || checkLine(r, c, 1, 0) || checkLine(r, c, 1, 1) || checkLine(r, c, 1, -1)) {
                    return true;
                }
            }
        }
    }
    return false;
  };

  const dropChip = (colIndex) => {
    if (winner !== 0) return;
    
    const newBoard = board.map(arr => [...arr]);
    let dropped = false;
    
    for (let r = ROWS - 1; r >= 0; r--) {
      if (newBoard[r][colIndex] === 0) { // 0 is empty, 9 is virus block
        newBoard[r][colIndex] = currentPlayer;
        dropped = true;
        break;
      }
    }

    if (!dropped) return;

    // Virus Mode Logic: 9 = Blocked/Virus
    if (gameMode.id === 'virus' && (turnCount + 1) % 3 === 0) {
      let placedBlock = false;
      let attempts = 0;
      while (!placedBlock && attempts < 20) {
        const rr = Math.floor(Math.random() * ROWS);
        const cc = Math.floor(Math.random() * COLS);
        if (newBoard[rr][cc] === 0) {
          newBoard[rr][cc] = 9; 
          placedBlock = true;
        }
        attempts++;
      }
    }

    setBoard(newBoard);
    
    if (checkWin(newBoard, currentPlayer)) {
      setWinner(currentPlayer);
    } else {
      setCurrentPlayer(currentPlayer === 1 ? 2 : 1);
      setTurnCount(prev => prev + 1);
    }
  };

  return { board, currentPlayer, winner, dropChip, resetGame, goToLobby };
};


// --- Screens ---

const LobbyScreen = ({ p1Char, setP1Char, p2Char, setP2Char, p1Skin, setP1Skin, p2Skin, setP2Skin, gameMode, setGameMode, goToGame }) => (
  <div className="flex-1 flex flex-col items-center justify-center p-4 relative z-10 w-full max-w-6xl mx-auto">
    
    <div className="z-10 text-center mb-8">
      <h1 className="text-6xl md:text-8xl font-black italic tracking-tighter text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-purple-500 mb-4"
          style={{ filter: 'drop-shadow(0 0 15px rgba(0,240,255,0.5))' }}>
        LOADOUT PROTOCOL
      </h1>
      
      <div className="inline-flex bg-slate-800/80 rounded-full p-1 border border-slate-600 shadow-xl">
        {GAME_MODES.map(mode => (
          <button
            key={mode.id}
            onClick={() => setGameMode(mode)}
            className={`px-4 py-2 rounded-full text-xs md:text-sm font-bold transition-all font-mono tracking-wider ${gameMode.id === mode.id ? 'bg-white text-black shadow-[0_0_15px_white]' : 'text-slate-400 hover:text-white'}`}
          >
            <Monitor size={14} className="inline mr-2" />{mode.name}
          </button>
        ))}
      </div>
      <div className="text-slate-400 text-xs mt-2 font-mono h-4">{gameMode.desc}</div>
    </div>

    {/* Selection Area */}
    <div className="flex flex-col lg:flex-row gap-8 z-10 w-full items-start justify-center">
      
      {/* P1 Section */}
      <PlayerLobbyCard player={1} char={p1Char} setChar={setP1Char} skin={p1Skin} setSkin={setP1Skin} />

      {/* VS Badge */}
      <div className="hidden lg:flex flex-col items-center justify-center h-full pt-20">
         <div className="text-6xl font-black text-slate-700 italic border-2 border-slate-700 p-2 rounded-full transform rotate-3">VS</div>
      </div>

      {/* P2 Section */}
      <PlayerLobbyCard player={2} char={p2Char} setChar={setP2Char} skin={p2Skin} setSkin={setP2Skin} />
    </div>

    {/* Start Button */}
    <button 
      onClick={goToGame}
      className="mt-12 group relative px-16 py-5 bg-transparent border-4 border-yellow-400 text-yellow-400 font-black tracking-[0.3em] text-2xl hover:bg-yellow-400 hover:text-black transition-all overflow-hidden z-10 rounded-lg shadow-[0_0_40px_rgba(250,204,21,0.5)]"
    >
      <span className="relative z-10">ENGAGE</span>
      <div className="absolute inset-0 bg-yellow-400 transform translate-y-full group-hover:translate-y-0 transition-transform duration-300" />
    </button>
  </div>
);

const PlayerLobbyCard = ({ player, char, setChar, skin, setSkin }) => {
  const color = char.color;

  const currentIndex = CHARACTERS.findIndex(c => c.id === char.id);
  
  const changeChar = (direction) => {
    let newIndex = currentIndex + direction;
    if (newIndex < 0) newIndex = CHARACTERS.length - 1;
    if (newIndex >= CHARACTERS.length) newIndex = 0;
    setChar(CHARACTERS[newIndex]);
  };

  const Icon = char.icon; // Destructure the Icon component

  return (
    <div className={`w-full lg:w-[450px] flex flex-col p-4 md:p-6 rounded-xl bg-slate-900/80 border-4 shadow-2xl backdrop-blur-sm`}
         style={{ borderColor: color, boxShadow: `0 0 25px ${color}40` }}>
      
      <div className={`text-xl font-black font-mono tracking-widest mb-4`} style={{ color }}>PLAYER {player} SELECTION</div>

      {/* Character Carousel */}
      <div className="flex items-center justify-between gap-4">
        <button onClick={() => changeChar(-1)} className={`text-white p-2 rounded-full hover:bg-white/10 transition-colors`} style={{ color }}>
          <ChevronLeft size={32} />
        </button>

        <div className="flex flex-col items-center flex-grow">
          <div className="w-40 h-40 md:w-56 md:h-56 rounded-full border-4 flex items-center justify-center relative shadow-[0_0_20px_currentColor]"
               style={{ borderColor: color, color }}>
            <Icon size={96} /> {/* Correctly render the Icon component */}
            <div className="absolute -bottom-2 text-sm font-mono tracking-widest bg-black/70 px-3 py-1 rounded-t-lg border-t border-x" style={{ borderColor: color }}>
              {char.role}
            </div>
          </div>
          <h2 className="text-4xl md:text-5xl font-black mt-4" style={{ color: color, textShadow: `0 0 10px ${color}` }}>{char.name}</h2>
          <p className="text-slate-400 text-sm italic mt-1 h-5">{char.desc}</p>
        </div>

        <button onClick={() => changeChar(1)} className={`text-white p-2 rounded-full hover:bg-white/10 transition-colors`} style={{ color }}>
          <ChevronRight size={32} /> {/* ChevronRight is now imported */}
        </button>
      </div>

      {/* Chip Skin Selector */}
      <div className="mt-8">
        <h3 className="text-lg font-mono text-slate-300 mb-3 border-b border-slate-700">CHIP SKIN (AMMO)</h3>
        <div className="flex justify-around bg-black/40 p-3 rounded-lg border border-white/10 backdrop-blur-sm">
          {CHIP_SKINS.map(skinOption => (
            <button
              key={skinOption.id}
              onClick={() => setSkin(skinOption)}
              className={`w-14 h-14 rounded-full flex items-center justify-center transition-all border-4 ${skin.id === skinOption.id ? 'bg-white/20' : 'border-transparent hover:bg-white/5'}`}
              style={{ borderColor: skin.id === skinOption.id ? color : 'transparent' }}
            >
              <skinOption.icon size={28} style={{ color: skin.id === skinOption.id ? color : '#666' }} />
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};


// --- Main App Component ---

export default function App() {
  // Global App State
  const [screen, setScreen] = useState('lobby'); // 'lobby' or 'game'
  const [offset, setOffset] = useState(0); // For background animation

  // Loadout State (Persists between games)
  const [p1Char, setP1Char] = useState(CHARACTERS[0]);
  const [p2Char, setP2Char] = useState(CHARACTERS[1]);
  const [p1Skin, setP1Skin] = useState(CHIP_SKINS[0]);
  const [p2Skin, setP2Skin] = useState(CHIP_SKINS[0]);
  const [gameMode, setGameMode] = useState(GAME_MODES[0]);
  const [scores, setScores] = useState({ 1: 0, 2: 0 });

  // Game Play State and Actions
  const { board, currentPlayer, winner, dropChip, resetGame } = useGameState(
    p1Char, p2Char, p1Skin, p2Skin, gameMode, () => setScreen('lobby')
  );

  // Background Animation Loop
  useEffect(() => {
    let frame;
    const animate = () => {
      setOffset(prev => (prev + 0.5) % 50); 
      frame = requestAnimationFrame(animate);
    };
    animate();
    return () => cancelAnimationFrame(frame);
  }, []);

  useEffect(() => {
      if (winner !== 0) {
          setScores(prev => ({ ...prev, [winner]: prev[winner] + 1 }));
      }
  }, [winner]);

  // Handle Game Screen
  if (screen === 'game') {
    const activePlayer = currentPlayer === 1 ? p1Char : p2Char;
    const opponentPlayer = currentPlayer === 2 ? p1Char : p2Char;
    const activeSkin = currentPlayer === 1 ? p1Skin.id : p2Skin.id;
    const activeColor = activePlayer.color;
    
    // --- Game Screen Render ---
    return (
      <div className="w-full h-screen overflow-hidden bg-[#050510] font-sans flex flex-col relative perspective-1000 select-none">
        <Background offset={offset} />
        
        {/* --- 1. The Cabinet Header (Marquee) --- */}
        <div className="relative z-10 w-full flex flex-col items-center pt-4 md:pt-8 animate-fade-in-down">
           <div className="relative group">
             <div className="absolute -inset-4 bg-purple-600/20 blur-xl rounded-full group-hover:bg-purple-600/40 transition-all"></div>
             <h1 className="text-5xl md:text-7xl font-black italic tracking-tighter text-transparent bg-clip-text bg-gradient-to-b from-white to-purple-400 relative z-10"
                 style={{ WebkitTextStroke: '2px #BD00FF', textShadow: '0 0 30px #BD00FF' }}>
               <span className="text-cyan-400" style={{ textShadow: '0 0 30px #00F0FF' }}>{gameMode.name.split(' ')[0]}</span> {gameMode.name.split(' ')[1]}
             </h1>
             <div className="absolute -bottom-6 w-full text-center text-sm font-mono tracking-[0.5em] text-yellow-400 animate-pulse">
               {winner !== 0 ? `WINNER: ${winner === 1 ? p1Char.name : p2Char.name}` : `TURN: ${activePlayer.name}`}
             </div>
           </div>
        </div>

        {/* --- 2. The Game Machine (Vertical Plane) --- */}
        <div className="flex-1 flex items-center justify-center relative z-20 px-4 py-2">
          {/* Machine Casing */}
          <div className="relative bg-[#1a1a2e] p-4 md:p-6 rounded-lg border-x-4 border-t-4 border-slate-700 shadow-[0_0_50px_rgba(0,0,0,0.8)]">
            
            {/* Neon Tubes */}
            <div className="absolute top-0 bottom-0 w-2 md:w-4 rounded-full shadow-[0_0_20px_#BD00FF] z-20 left-[-10px]" style={{ backgroundColor: COLORS.purple, boxShadow: `0 0 10px ${COLORS.purple}, 0 0 20px ${COLORS.purple}` }}/>
            <div className="absolute top-0 bottom-0 w-2 md:w-4 rounded-full shadow-[0_0_20px_#BD00FF] z-20 right-[-10px]" style={{ backgroundColor: COLORS.purple, boxShadow: `0 0 10px ${COLORS.purple}, 0 0 20px ${COLORS.purple}` }}/>

            {/* The Board Itself */}
            <div className="bg-black rounded border-4 border-slate-600 p-2 shadow-inner relative overflow-hidden">
               
               {/* Click Detectors */}
               <div className="absolute inset-0 flex z-40">
                 {Array(7).fill(0).map((_, i) => (
                   <div key={i} onClick={() => dropChip(i)} className="flex-1 h-full cursor-pointer hover:bg-white/5 transition-colors" />
                 ))}
               </div>

               {/* Slots */}
               <div className="grid grid-cols-7 gap-1 md:gap-2">
               {board.map((row, r) => (
                 row.map((cell, c) => (
                   <div key={`${r}-${c}`} className="w-8 h-8 md:w-12 md:h-12 bg-[#0f0f15] rounded-full shadow-[inset_0_2px_4px_black] flex items-center justify-center relative border border-slate-800">
                     
                     {cell === 9 && ( // Virus Block
                        <div className="w-full h-full bg-red-900/50 rounded-full flex items-center justify-center animate-pulse">
                           <Skull size={20} className="text-red-500" />
                        </div>
                     )}

                     {cell !== 0 && cell !== 9 && (
                       <div 
                         className={`w-[90%] h-[90%] rounded-full animate-in fade-in zoom-in shadow-lg flex items-center justify-center transition-all duration-300`}
                         style={{ 
                           backgroundColor: cell === 1 ? p1Char.secondary : p2Char.secondary,
                           color: cell === 1 ? p1Char.color : p2Char.color,
                           boxShadow: `0 0 15px ${cell === 1 ? p1Char.color : p2Char.color}`
                         }}
                       >
                         <ChipRenderer 
                            skin={cell === 1 ? p1Skin.id : p2Skin.id} 
                            color={cell === 1 ? p1Char.color : p2Char.color} 
                         />
                       </div>
                     )}
                   </div>
                 ))
               ))}
               </div>
            </div>
          </div>
        </div>

        {/* --- 3. The Control Deck (Angled Plane) --- */}
        <div 
          className="w-full h-[280px] md:h-[350px] bg-gradient-to-b from-[#1e1e2e] to-[#0f0f15] relative z-30 flex flex-col border-t-4 border-slate-600"
          style={{ 
            transform: 'perspective(1000px) rotateX(15deg) translateY(-20px)',
            transformOrigin: 'top center',
            boxShadow: '0 -20px 50px rgba(0,0,0,0.8)'
          }}
        >
          {/* Deck Texture */}
          <div className="absolute inset-0 opacity-10 bg-[radial-gradient(circle_at_center,_#fff_1px,_transparent_1px)] bg-[length:10px_10px]"></div>

          {/* Control Panel Content */}
          <div className="flex-1 flex items-center justify-around px-2 md:px-10 pb-8 transform translate-y-4">
             
             {/* Player 1 Station */}
             <div className="transform -skew-x-6 rotate-y-12 hidden md:block">
               <PlayerDisplay player={1} char={p1Char} score={scores[1]} active={currentPlayer === 1 && winner === 0} />
             </div>

             {/* Central Buttons */}
             <div className="flex items-center gap-4 md:gap-8 bg-[#111] p-4 md:p-6 rounded-3xl border border-slate-700 shadow-[inset_0_0_20px_black]">
                <ArcadeButton icon={Menu} color="purple" onClick={() => setScreen('lobby')} label="LOADOUT" />
                
                <div className="relative -top-4">
                   <ArcadeButton 
                     icon={winner ? Play : RotateCcw} 
                     color={winner ? "green" : "yellow"} 
                     onClick={resetGame} 
                     label={winner ? "REMATCH" : "RESET"} 
                     size="lg" 
                   />
                </div>

                <ArcadeButton icon={Trophy} color="red" onClick={() => setScreen('lobby')} label="EXIT" />
             </div>

             {/* Player 2 Station */}
             <div className="transform skew-x-6 rotate-y-12 hidden md:block">
               <PlayerDisplay player={2} char={p2Char} score={scores[2]} active={currentPlayer === 2 && winner === 0} />
             </div>
          </div>

          {/* Mobile-Only Score Display */}
          <div className="md:hidden flex justify-between px-6 pb-6 transform -skew-x-12">
             <div className={`text-cyan-400 font-mono font-bold ${currentPlayer === 1 ? 'animate-pulse' : 'opacity-50'}`}>P1 Score: {scores[1]}</div>
             <div className={`text-pink-500 font-mono font-bold ${currentPlayer === 2 ? 'animate-pulse' : 'opacity-50'}`}>P2 Score: {scores[2]}</div>
          </div>

        </div>

        {/* Global Glow Overlay (Bloom) */}
        <div className="absolute inset-0 pointer-events-none z-50 mix-blend-screen bg-gradient-to-b from-purple-900/10 via-transparent to-cyan-900/10"></div>

      </div>
    );
  }

  // --- Lobby Screen Render ---
  return (
    <div className="w-full h-screen overflow-hidden bg-[#050510] font-sans flex flex-col relative perspective-1000 select-none">
       <Background offset={offset} />
       <LobbyScreen 
          p1Char={p1Char} setP1Char={setP1Char}
          p2Char={p2Char} setP2Char={setP2Char}
          p1Skin={p1Skin} setP1Skin={setP1Skin}
          p2Skin={p2Skin} setP2Skin={setP2Skin}
          gameMode={gameMode} setGameMode={setGameMode}
          goToGame={() => setScreen('game')}
       />
    </div>
  );
}