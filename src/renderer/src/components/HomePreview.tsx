import { useEffect, useRef, useState, type CSSProperties, type ReactNode } from 'react'
import type { HomeConfig, QuickEntry, QuickId, TvApp } from '@shared/api'
import couchNight from '../assets/couch/night.jpg'
import couchNightFg from '../assets/couch/night-fg.webp'
import couchNightGlass from '../assets/couch/night-glass.jpg'

// The launcher lays out in dp on a 960x540 screen; the preview draws at that size and scales to fit.
const W = 960
const H = 540
const SAFE_X = 48
const SAFE_Y = 27
const GAP = 16

const QUICK_GLYPH: Record<QuickId, string> = {
  inputs: 'M4 7h12v10H4zM16 10h4M16 14h4',
  wifi: 'M4 9a12 12 0 0 1 16 0M7 12.5a7.5 7.5 0 0 1 10 0M10 16a3 3 0 0 1 4 0',
  bluetooth: 'M8 7l8 8-4 3V6l4 3-8 8',
  display: 'M4 6h16v10H4zM9 20h6',
  apps: 'M5 5h4v4H5zM15 5h4v4h-4zM5 15h4v4H5zM15 15h4v4h-4z',
  sleep: 'M17 14A7 7 0 0 1 10 5a7 7 0 1 0 7 9z',
  search: 'M11 5a6 6 0 1 1 0 12 6 6 0 0 1 0-12zM16 16l4 4',
  music: 'M9 18V6l10-2v12M9 18a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0zM19 16a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0z',
  settings: 'M12 9a3 3 0 1 1 0 6 3 3 0 0 1 0-6zM12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M5.6 18.4l2.1-2.1M16.3 7.7l2.1-2.1',
  panel: 'M4 7h10M18 7h2M4 17h4M12 17h8M14 5v4M8 15v4',
  themes: 'M12 4a8 8 0 1 0 0 16c1 0 1.5-.8 1.5-1.6 0-1.4 1-2.4 2.4-2.4H18a2 2 0 0 0 2-2A8 8 0 0 0 12 4zM8 11h.01M11 8h.01M15 8h.01',
  fireplace: 'M12 21c-3.5 0-6-2.4-6-5.6 0-3.2 2.6-5 3.5-8.4 1.8 1.4 2.6 3 2.6 4.6 1-.7 1.6-1.8 1.7-3 1.6 1.6 2.2 3.6 2.2 5.6 0 3.4-2.5 6.8-4 6.8z'
}

export const QUICK_LABEL: Record<QuickId, string> = {
  inputs: 'Inputs', wifi: 'Wi-Fi', bluetooth: 'Bluetooth', display: 'Picture & sound', apps: 'Apps', sleep: 'Screensaver', search: 'Search', music: 'Music bars', settings: 'Settings',
  panel: 'Quick settings', themes: 'Themes', fireplace: 'Fireplace'
}

export function QuickIcon({ id, size = 16 }: { id: QuickId; size?: number }): ReactNode {
  return (
    <svg viewBox="0 0 24 24" width={size} height={size} fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" aria-hidden>
      <path d={QUICK_GLYPH[id]} />
    </svg>
  )
}

/** The package of an app shortcut, or null for a quick-settings action. */
export const quickApp = (q: QuickEntry): string | null => (q.startsWith('app:') ? q.slice(4) : null)

export function quickLabel(q: QuickEntry, apps: TvApp[]): string {
  const pkg = quickApp(q)
  return pkg ? (apps.find((a) => a.pkg === pkg)?.label ?? pkg) : QUICK_LABEL[q as QuickId]
}

/** A stand-in for an app's own icon, which the desktop app can't read: its first letter on a disc. */
function AppMark({ label, size }: { label: string; size: number }): ReactNode {
  return (
    <span aria-hidden style={{ width: size, height: size, borderRadius: size / 2, background: '#D7CFC4', color: '#1C2A1F', display: 'inline-grid', placeItems: 'center', fontSize: size * 0.55, fontWeight: 600, lineHeight: 1, flex: 'none' }}>
      {label.trim().charAt(0).toUpperCase() || '?'}
    </span>
  )
}

/** The icon a top-bar entry shows: the drawn glyph, or for an app a disc 4 px larger, as on the TV. */
export function QuickMark({ q, apps, size = 16 }: { q: QuickEntry; apps: TvApp[]; size?: number }): ReactNode {
  return quickApp(q) ? <AppMark label={quickLabel(q, apps)} size={size + 4} /> : <QuickIcon id={q as QuickId} size={size} />
}

/** The order and grouping the launcher uses, from the same settings. */
function model(apps: TvApp[], cfg: HomeConfig) {
  const by = new Map(apps.map((a) => [a.pkg, a]))
  const ordered = [...cfg.order.flatMap((p) => (by.has(p) ? [by.get(p)!] : [])), ...apps.filter((a) => !cfg.order.includes(a.pkg)).sort((a, b) => a.label.localeCompare(b.label))].filter(
    (a) => !cfg.hidden.includes(a.pkg)
  )
  const inFolders = new Set(cfg.folders.flatMap((f) => f.apps))
  const dock = ordered.filter((a) => !inFolders.has(a.pkg)).slice(0, cfg.dock_size)
  const grid = ordered.filter((a) => !inFolders.has(a.pkg) && !dock.includes(a))
  const rows = cfg.rows.map((r) => ({ name: r.name, apps: r.apps.flatMap((p) => (by.has(p) ? [by.get(p)!] : [])) })).filter((r) => r.apps.length)
  const folders = cfg.folders.map((f) => ({ name: f.name, apps: f.apps.flatMap((p) => (by.has(p) ? [by.get(p)!] : [])) })).filter((f) => f.apps.length)
  return { dock, grid, rows, folders }
}

function Tile({ app, w, names, focused, cfg }: { app: TvApp; w: number; names: boolean; focused?: boolean; cfg: HomeConfig }): ReactNode {
  const ring: CSSProperties = focused
    ? cfg.focus === 'glow'
      ? { boxShadow: `0 0 0 2px #F0EBE5, 0 0 16px 4px ${cfg.accent}`, transform: 'scale(1.08)' }
      : cfg.focus === 'outline'
        ? { outline: `3px solid ${cfg.accent}`, outlineOffset: 0, transform: 'scale(1.08)' }
        : cfg.focus === 'lift'
          ? { boxShadow: `0 10px 18px -4px rgba(0,0,0,.6), 0 0 22px 1px ${cfg.accent}59, inset 0 0 0 1px rgba(255,255,255,.28)`, transform: 'scale(1.09) translateY(-3px)' }
          : {}
    : {}
  // The icon styles: square, squircle or round tiles, a little narrower; the logo shows on the TV, the
  // preview draws the first letter in its place.
  const icons = cfg.icon_style !== 'banner'
  const shape = icons ? cfg.tile_shape : 'wide'
  const side = shape === 'wide' ? w : w * 0.72
  const radius = shape === 'round' ? '50%' : shape === 'squircle' ? '32%' : shape === 'square' ? '22%' : 10
  const fill = cfg.icon_style === 'mono' ? 'rgba(255,255,255,.12)' : cfg.icon_style === 'duo' ? `color-mix(in srgb, ${cfg.background} 70%, ${cfg.accent})` : '#2D3A2E'
  const ink = cfg.icon_style === 'mono' || cfg.icon_style === 'duo' ? `color-mix(in srgb, ${cfg.accent} 65%, white)` : '#F0EBE5'
  return (
    <div style={{ width: side, flex: 'none' }}>
      <div style={{ height: shape === 'wide' ? (w * 9) / 16 : side, borderRadius: radius, background: fill, display: 'grid', placeItems: 'center', color: ink, fontWeight: icons ? 600 : 500, fontSize: icons && shape !== 'wide' ? side * 0.4 : Math.max(12, w / 11), padding: '0 8px', textAlign: 'center', lineHeight: 1.15, overflow: 'hidden', ...ring }}>
        {icons ? app.label.charAt(0).toUpperCase() : app.label}
      </div>
      {names && <div style={{ visibility: focused || !cfg.name_focus ? 'visible' : 'hidden', marginTop: 6, fontSize: 14, color: '#F0EBE5', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', textAlign: shape === 'wide' ? 'left' : 'center' }}>{app.label}</div>}
    </div>
  )
}

function Folder({ name, apps, w }: { name: string; apps: TvApp[]; w: number }): ReactNode {
  return (
    <div style={{ width: w, flex: 'none' }}>
      <div style={{ height: (w * 9) / 16, borderRadius: 10, background: 'rgba(45,58,46,0.9)', padding: 7, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 5 }}>
        {apps.slice(0, 4).map((a) => (
          <div key={a.pkg} style={{ borderRadius: 4, background: '#3A4A3B' }} />
        ))}
      </div>
      <div style={{ marginTop: 6, fontSize: 14, color: '#F0EBE5' }}>
        {name} · {apps.length}
      </div>
    </div>
  )
}

function Title({ children }: { children: ReactNode }): ReactNode {
  return <div style={{ fontSize: 18, color: '#F0EBE5', fontWeight: 500, margin: '12px 0 4px' }}>{children}</div>
}

function Sections({ apps, cfg, width, dockW, showFolders = true }: { apps: TvApp[]; cfg: HomeConfig; width: number; dockW: number; showFolders?: boolean }): ReactNode {
  const m = model(apps, cfg)
  const gridW = (width - GAP * (cfg.columns - 1)) / cfg.columns
  const rowW = 148 * (cfg.tile_scale / 100)
  let focused = false
  const focusOnce = (): boolean => (focused ? false : (focused = true))
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
      {cfg.sections.map((s) => {
        // A stand-in: on the TV the card shows only while an app has something playing.
        if (s === 'nowplaying')
          return (
            <div key={s} style={{ display: 'flex', alignItems: 'center', gap: 14, width: 520, padding: 12, margin: '8px 0', borderRadius: 14, background: 'rgba(45,58,46,0.88)' }}>
              <div style={{ width: 64, height: 64, borderRadius: 8, background: '#3A4A3B', flex: 'none' }} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 17, fontWeight: 500 }}>What's playing</div>
                <div style={{ fontSize: 13, color: '#D7CFC4', marginTop: 3 }}>Artist · App, while something plays</div>
              </div>
              {['❚❚', '▸▸'].map((g) => (
                <div key={g} style={{ width: 44, height: 44, borderRadius: 22, background: 'rgba(255,255,255,0.08)', display: 'grid', placeItems: 'center', fontSize: 13 }}>{g}</div>
              ))}
            </div>
          )
        if (s === 'dock' && m.dock.length)
          return (
            <div key={s} style={{ display: 'flex', gap: GAP, padding: '8px 0' }}>
              {m.dock.map((a, i) => <Tile key={a.pkg} app={a} w={dockW * (cfg.dock_scale / 100)} names={cfg.show_names} focused={i === 0 && focusOnce()} cfg={cfg} />)}
            </div>
          )
        if (s === 'rows')
          return m.rows.map((r, ri) => (
            <div key={`row${ri}`}>
              <Title>{r.name}</Title>
              <div style={{ display: 'flex', gap: GAP, padding: '6px 0' }}>
                {r.apps.map((a, i) => <Tile key={a.pkg} app={a} w={rowW} names={cfg.show_names} focused={ri === 0 && i === 0 && focusOnce()} cfg={cfg} />)}
              </div>
            </div>
          ))
        if (s === 'folders' && showFolders && m.folders.length)
          return (
            <div key={s}>
              <Title>Folders</Title>
              <div style={{ display: 'flex', gap: GAP, padding: '6px 0' }}>
                {m.folders.map((f) => <Folder key={f.name} name={f.name} apps={f.apps} w={rowW} />)}
              </div>
            </div>
          )
        if (s === 'grid' && m.grid.length)
          return (
            <div key={s}>
              <Title>{m.dock.length || m.rows.length ? 'All apps' : 'Apps'}</Title>
              <div style={{ display: 'grid', gridTemplateColumns: `repeat(${cfg.columns}, ${gridW}px)`, gap: `14px ${GAP}px`, padding: '6px 0' }}>
                {m.grid.map((a, i) => <Tile key={a.pkg} app={a} w={gridW} names={cfg.show_names} focused={i === 0 && focusOnce()} cfg={cfg} />)}
              </div>
            </div>
          )
        return null
      })}
    </div>
  )
}

function Clock({ cfg, size, dateSize, align }: { cfg: HomeConfig; size: number; dateSize: number; align: 'left' | 'right' }): ReactNode {
  if (cfg.clock === 'off') return null
  return (
    <div style={{ textAlign: align }}>
      <div style={{ fontSize: size, fontWeight: 300, lineHeight: 1.1, fontFamily: 'var(--font)' }}>9:41</div>
      {cfg.clock === 'time_date' && <div style={{ fontSize: dateSize, color: '#D7CFC4', fontFamily: 'var(--font)' }}>Wednesday, 28 October</div>}
      {cfg.weather_place && (
        <div style={{ fontSize: dateSize, color: '#D7CFC4', fontFamily: 'var(--font)', marginTop: 4 }}>
          ☁ 14°  16° / 9°  {cfg.weather_place}
        </div>
      )}
    </div>
  )
}

/** The Mac layout's menu bar: half the top safe margin, then 30 dp of content (HomeScreen.kt, MENU_H). */
const MENU_H = SAFE_Y / 2 + 30

/** As on the TV: the dock draws icons, so banner tiles become the colour style and wide tiles squircles. */
const macLook = (cfg: HomeConfig): HomeConfig => ({
  ...cfg,
  icon_style: cfg.icon_style === 'banner' ? 'colour' : cfg.icon_style,
  tile_shape: cfg.tile_shape === 'wide' ? 'squircle' : cfg.tile_shape
})

/**
 * Mac: the menu bar, open wallpaper, and the glass dock centred at the bottom, drawn with its first app
 * focused so the magnification and the name bubble show.
 */
function MacBody({ apps, cfg, quick, greet }: { apps: TvApp[]; cfg: HomeConfig; quick: QuickEntry[]; greet: string | null }): ReactNode {
  const m = model(apps, cfg)
  const look = macLook(cfg)
  const base = 56 * (cfg.dock_scale / 100)
  const radius = look.tile_shape === 'round' ? '50%' : look.tile_shape === 'squircle' ? '32%' : '22%'
  const focusAt = m.dock.length ? 1 : 0
  const scaleAt = (i: number): number => [1.5, 1.24, 1.08][Math.abs(i - focusAt)] ?? 1
  const item = (i: number, key: string, label: string, node: ReactNode): ReactNode => {
    const s = scaleAt(i)
    return (
      <div key={key} style={{ position: 'relative', flex: 'none', padding: `0 ${5 + (base * (s - 1)) / 2}px` }}>
        <div style={{ width: base, height: base, transform: `scale(${s})`, transformOrigin: '50% 100%' }}>{node}</div>
        {i === focusAt && (
          <>
            <div style={{ position: 'absolute', left: '50%', bottom: base * s + 10, transform: 'translateX(-50%)', whiteSpace: 'nowrap', fontSize: 14, padding: '4px 10px', borderRadius: 7, background: 'rgba(30,30,36,0.9)' }}>{label}</div>
            <div style={{ position: 'absolute', left: '50%', bottom: -7, width: 4, height: 4, marginLeft: -2, borderRadius: 2, background: 'rgba(240,235,229,0.85)' }} />
          </>
        )}
      </div>
    )
  }
  const launchpad = (
    <div style={{ width: base, height: base, borderRadius: radius, background: 'linear-gradient(135deg, #9AA6BC, #4B5567)', display: 'grid', placeItems: 'center' }}>
      <QuickIcon id="apps" size={base * 0.5} />
    </div>
  )
  const folder = (n: number): ReactNode => (
    <div style={{ width: base, height: base, borderRadius: radius, background: 'rgba(255,255,255,0.22)', padding: 7, boxSizing: 'border-box', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 4 }}>
      {Array.from({ length: Math.min(4, n) }, (_, k) => <div key={k} style={{ borderRadius: 4, background: 'rgba(58,74,59,0.9)' }} />)}
    </div>
  )
  return (
    <div style={{ position: 'relative', height: '100%', overflow: 'hidden', background: `linear-gradient(transparent 60%, ${cfg.background}73)` }}>
      <div style={{ height: MENU_H, boxSizing: 'border-box', padding: `${SAFE_Y / 2}px ${SAFE_X - 6}px 0 ${SAFE_X}px`, display: 'flex', alignItems: 'center', gap: 16, fontSize: 15, background: 'rgba(0,0,0,0.32)', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
        <svg viewBox="0 0 24 24" width={17} height={17} fill="none" stroke="currentColor" strokeWidth={1.8} aria-hidden>
          <path d="M3 6h18v11H3zM8 21h8" />
        </svg>
        <span style={{ fontWeight: 600 }}>{m.dock[0]?.label ?? 'Launchpad'}</span>
        {greet && <span style={{ color: '#D7CFC4', fontSize: 14 }}>{greet}</span>}
        <span style={{ flex: 1 }} />
        {quick.map((q) => (
          <span key={q} style={{ width: 34, display: 'grid', placeItems: 'center' }}>
            <QuickMark q={q} apps={apps} size={16} />
          </span>
        ))}
        {cfg.weather_place && <span>☁ 14°</span>}
        {cfg.clock !== 'off' && <span style={{ fontWeight: 500, whiteSpace: 'nowrap' }}>{cfg.clock === 'time_date' ? 'Wed 28 Oct   9:41' : '9:41'}</span>}
      </div>
      {cfg.sections.includes('nowplaying') && (
        <div style={{ position: 'absolute', left: SAFE_X, top: MENU_H + 20, display: 'flex', alignItems: 'center', gap: 12, width: 360, padding: 12, borderRadius: 14, background: 'rgba(45,58,46,0.88)' }}>
          <div style={{ width: 48, height: 48, borderRadius: 8, background: '#3A4A3B', flex: 'none' }} />
          <div style={{ fontSize: 14 }}>
            <div style={{ fontWeight: 500 }}>What's playing</div>
            <div style={{ color: '#D7CFC4', fontSize: 12, marginTop: 2 }}>Shows while something plays</div>
          </div>
        </div>
      )}
      <div style={{ position: 'absolute', left: 0, right: 0, bottom: SAFE_Y, display: 'flex', justifyContent: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'flex-end', padding: '7px 8px', borderRadius: 22, background: 'rgba(255,255,255,0.16)', border: '1px solid rgba(255,255,255,0.22)' }}>
          {item(0, 'launchpad', 'Launchpad', launchpad)}
          {m.dock.map((a, i) => item(i + 1, a.pkg, a.label, <Tile app={a} w={base / 0.72} names={false} cfg={look} />))}
          {m.folders.length > 0 && <div style={{ width: 1, height: base, margin: '0 6px', background: 'rgba(255,255,255,0.3)', flex: 'none' }} />}
          {m.folders.map((f, j) => item(m.dock.length + 1 + j, `folder:${f.name}`, f.name, folder(f.apps.length)))}
        </div>
      </div>
    </div>
  )
}

/** couch-tv's glass: a smoky tint with a light edge (HomeScreen.kt, COUCH_GLASS and COUCH_EDGE). */
const COUCH_GLASS = 'rgba(30, 20, 28, 0.32)'
const COUCH_EDGE = '1px solid rgba(255, 210, 220, 0.16)'

/**
 * Couch: the Night scene (the TV turns through five), with the glass clock behind the peak, the date above,
 * the greeting and quick settings at the top, and the glass dock at the bottom ending in All apps. Sizes are
 * in rem as on the TV: a 120th of the width, 8 here.
 */
function CouchBody({ apps, cfg, quick, greet }: { apps: TvApp[]; cfg: HomeConfig; quick: QuickEntry[]; greet: string | null }): ReactNode {
  const m = model(apps, cfg)
  const look = macLook(cfg)
  const R = 8
  const w = 60 * (cfg.dock_scale / 100)
  const radius = look.tile_shape === 'round' ? '50%' : look.tile_shape === 'squircle' ? '32%' : '22%'
  const cover: CSSProperties = { position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover', filter: 'brightness(0.86)' }
  const top = 9.375 * R
  const size = 25 * R
  return (
    <div style={{ position: 'relative', height: '100%', overflow: 'hidden' }}>
      <img src={couchNight} alt="" style={cover} />
      {cfg.clock !== 'off' && (
        <div style={{ position: 'absolute', left: 0, right: 0, top, textAlign: 'center', whiteSpace: 'nowrap', fontFamily: 'Antonio, "Arial Narrow", sans-serif', fontWeight: 300, fontSize: size, lineHeight: 0.82, letterSpacing: '-0.02em' }}>
          {/* the glass: the blurred copy, clipped to the digits and lined up with the photo behind them */}
          <span style={{ display: 'block', padding: '0.2em 0', margin: '-0.2em 0', color: 'transparent', backgroundImage: `url(${couchNightGlass})`, backgroundSize: '960px 540px', backgroundRepeat: 'no-repeat', backgroundPosition: `0 ${-(top - size * 0.2)}px`, WebkitBackgroundClip: 'text', backgroundClip: 'text', filter: 'brightness(1.15) saturate(1.2)', opacity: 0.9 }}>
            9:41
          </span>
          <span style={{ position: 'absolute', inset: 0, color: 'rgba(255, 200, 215, 0.06)', WebkitTextStroke: '0.72px rgba(255, 215, 225, 0.5)' }}>9:41</span>
        </div>
      )}
      <img src={couchNightFg} alt="" style={cover} />
      {cfg.clock === 'time_date' && (
        <div style={{ position: 'absolute', left: 0, right: 0, top: 6 * R, textAlign: 'center', fontSize: 2.125 * R, fontWeight: 500, color: 'rgba(255, 232, 238, 0.92)', textShadow: '0 1px 9px rgba(0, 0, 0, 0.25)', whiteSpace: 'nowrap' }}>
          Wednesday 28 October{cfg.weather_place ? '   ☁ 14°' : ''}
        </div>
      )}
      <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(transparent 60%, rgba(0, 0, 0, 0.5))' }} />
      {greet && (
        <div style={{ position: 'absolute', left: SAFE_X, top: SAFE_Y, fontSize: 16, fontWeight: 500, padding: '8px 18px', borderRadius: 999, background: COUCH_GLASS, border: COUCH_EDGE, whiteSpace: 'nowrap' }}>{greet}</div>
      )}
      <div style={{ position: 'absolute', right: SAFE_X, top: SAFE_Y, display: 'flex', gap: 12 }}>
        {quick.map((q) => (
          <span key={q} style={{ width: 40, height: 40, borderRadius: '50%', display: 'grid', placeItems: 'center', background: 'rgba(255, 255, 255, 0.08)' }}>
            <QuickMark q={q} apps={apps} size={22} />
          </span>
        ))}
      </div>
      <div style={{ position: 'absolute', left: 0, right: 0, bottom: SAFE_Y, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        {cfg.sections.includes('nowplaying') && (
          <div style={{ alignSelf: 'stretch', padding: `0 0 10px ${SAFE_X}px` }}>
            <div style={{ fontSize: 13, fontWeight: 600, color: 'rgba(240, 235, 229, 0.78)', marginBottom: 6 }}>Now playing</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, width: 360, padding: 12, borderRadius: 14, background: 'rgba(45, 58, 46, 0.88)' }}>
              <div style={{ width: 48, height: 48, borderRadius: 8, background: '#3A4A3B', flex: 'none' }} />
              <div style={{ fontSize: 13, color: '#D7CFC4' }}>Shows while something plays</div>
            </div>
          </div>
        )}
        <div style={{ display: 'flex', gap: 13, padding: '12px 14px', borderRadius: 26, background: COUCH_GLASS, border: COUCH_EDGE, maxWidth: 960 - SAFE_X * 2, overflow: 'hidden' }}>
          {m.dock.map((a) => (
            <div key={a.pkg} style={{ width: w, flex: 'none' }}>
              <Tile app={a} w={w / 0.72} names={false} cfg={look} />
            </div>
          ))}
          {m.folders.map((f) => (
            <div key={f.name} style={{ width: w, height: w, flex: 'none', borderRadius: radius, background: 'rgba(255, 255, 255, 0.12)', padding: 7, boxSizing: 'border-box', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 4 }}>
              {f.apps.slice(0, 4).map((a) => <div key={a.pkg} style={{ borderRadius: 4, background: 'rgba(58, 74, 59, 0.9)' }} />)}
            </div>
          ))}
          <div style={{ width: w, height: w, flex: 'none', borderRadius: radius, background: 'rgba(255, 255, 255, 0.12)', display: 'grid', placeItems: 'center', fontSize: 22, letterSpacing: 2 }}>•••</div>
        </div>
      </div>
    </div>
  )
}

const greetingText = (cfg: HomeConfig): string | null => (cfg.greeting.trim() ? `Good evening, ${cfg.greeting.trim()}` : null)

/**
 * A picture of the TV's home screen from the same settings the launcher reads: layout, sections, sizes
 * and colours. App art is a coloured monogram, since the desktop app can't read the TV's banners.
 */
export function HomePreview({ apps, cfg }: { apps: TvApp[]; cfg: HomeConfig }): ReactNode {
  const box = useRef<HTMLDivElement>(null)
  const [scale, setScale] = useState(0.5)
  useEffect(() => {
    const el = box.current
    if (!el) return
    const ro = new ResizeObserver(() => setScale(el.clientWidth / W))
    ro.observe(el)
    return () => ro.disconnect()
  }, [])
  const greet = greetingText(cfg)
  // As on the TV: icons it can't open, and shortcuts to apps that aren't installed, are left out.
  const quick = cfg.quick.filter((q) => {
    const pkg = quickApp(q)
    return pkg ? apps.some((a) => a.pkg === pkg) : !cfg.quick_available || cfg.quick_available.includes(q as QuickId)
  })
  const ground: CSSProperties = {
    background: cfg.has_wallpaper
      ? `linear-gradient(rgba(0,0,0,${cfg.wallpaper_dim / 100}), rgba(0,0,0,${cfg.wallpaper_dim / 100})), linear-gradient(160deg, #6b5a3e, #2f3b2a 55%, #1a2418)`
      : cfg.background
  }
  let body: ReactNode
  if (cfg.layout === 'split') {
    body = (
      <div style={{ display: 'flex', height: '100%', background: 'linear-gradient(90deg, rgba(28,42,31,0.7), transparent 60%)' }}>
        <div style={{ width: W * 0.32, padding: `${SAFE_Y + 24}px 0 ${SAFE_Y + 48}px ${SAFE_X}px`, display: 'flex', flexDirection: 'column', gap: 14 }}>
          <Clock cfg={cfg} size={56} dateSize={15} align="left" />
          {greet && <div style={{ fontSize: 18, fontWeight: 500 }}>{greet}</div>}
          <div style={{ flex: 1 }} />
          {quick.map((q) => (
            <div key={q} style={{ display: 'flex', alignItems: 'center', gap: 12, fontSize: 15 }}>
              <span style={{ width: 22, display: 'inline-grid', placeItems: 'center' }}>
                <QuickMark q={q} apps={apps} size={18} />
              </span>{' '}
              {quickLabel(q, apps)}
            </div>
          ))}
        </div>
        <div style={{ flex: 1, padding: `${SAFE_Y + 12}px ${SAFE_X}px 0 0`, overflow: 'hidden' }}>
          <Sections apps={apps} cfg={cfg} width={W * 0.68 - SAFE_X} dockW={168} />
        </div>
      </div>
    )
  } else if (cfg.layout === 'rail') {
    body = (
      <div style={{ display: 'flex', height: '100%' }}>
        <div style={{ width: SAFE_X + 56, padding: `${SAFE_Y + 8}px 0 0 ${SAFE_X + 7}px`, display: 'flex', flexDirection: 'column', gap: 18, background: 'linear-gradient(90deg, rgba(28,42,31,0.6), rgba(28,42,31,0.1))' }}>
          {quick.map((q) => (
            <span key={q} style={{ width: 22, height: 22, display: 'grid', placeItems: 'center' }}>
              <QuickMark q={q} apps={apps} size={22} />
            </span>
          ))}
          {cfg.folders.length > 0 && <div style={{ height: 6 }} />}
          {cfg.folders.map((f) => (
            <svg key={f.name} viewBox="0 0 24 24" width={22} height={22} fill="none" stroke="currentColor" strokeWidth={1.8}>
              <path d="M3 7h6l2 2h10v10H3z" />
            </svg>
          ))}
        </div>
        <div style={{ flex: 1, padding: `${SAFE_Y}px ${SAFE_X}px 0 24px`, overflow: 'hidden' }}>
          <div style={{ display: 'flex', alignItems: 'center', marginBottom: 20 }}>
            <div style={{ fontSize: 20, fontWeight: 500, flex: 1 }}>{greet}</div>
            <Clock cfg={cfg} size={26} dateSize={12} align="right" />
          </div>
          <Sections apps={apps} cfg={cfg} width={W - SAFE_X - 56 - 24 - SAFE_X} dockW={196} showFolders={false} />
        </div>
      </div>
    )
  } else if (cfg.layout === 'mac') {
    body = <MacBody apps={apps} cfg={cfg} quick={quick} greet={greet} />
  } else if (cfg.layout === 'couch') {
    body = <CouchBody apps={apps} cfg={cfg} quick={quick} greet={greet} />
  } else {
    body = (
      <div style={{ height: '100%', padding: `${SAFE_Y}px ${SAFE_X}px 0`, overflow: 'hidden', background: 'linear-gradient(rgba(28,42,31,0.6), transparent 210px)' }}>
        <div style={{ display: 'flex', alignItems: 'flex-start' }}>
          <div style={{ fontSize: 20, fontWeight: 500, flex: 1, paddingTop: 4 }}>{greet}</div>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 10 }}>
            <Clock cfg={cfg} size={26} dateSize={12} align="right" />
            {quick.length > 0 && (
              <div style={{ display: 'flex', gap: 10 }}>
                {quick.map((q) => (
                  <span key={q} style={{ width: 40, height: 40, borderRadius: 20, background: 'rgba(255,255,255,0.08)', display: 'grid', placeItems: 'center' }}>
                    <QuickMark q={q} apps={apps} size={22} />
                  </span>
                ))}
              </div>
            )}
          </div>
        </div>
        <div style={{ height: (H * cfg.top_space) / 100 }} />
        <Sections apps={apps} cfg={cfg} width={W - SAFE_X * 2} dockW={200} />
      </div>
    )
  }
  return (
    <div ref={box} className="home-preview" style={{ height: H * scale }} aria-label="Preview of the TV home screen">
      <div style={{ width: W, height: H, transform: `scale(${scale})`, transformOrigin: '0 0', color: '#F0EBE5', fontFamily: 'Roboto, system-ui, sans-serif', ...ground }}>{body}</div>
      {/* The Mac and Couch docks fill the bottom middle, so the note moves up: under the menu bar, or above the dock. */}
      <span
        className="home-preview-note"
        style={cfg.layout === 'mac' ? { bottom: 'auto', top: (MENU_H + 8) * scale } : cfg.layout === 'couch' ? { bottom: (SAFE_Y + 175) * scale } : undefined}
      >
        Sample time and greeting · app art shows on the TV
      </span>
    </div>
  )
}
