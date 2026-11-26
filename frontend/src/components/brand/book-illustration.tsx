/**
 * Quiet line-art for the login panel: a shelf of spines beside an open book. Single stroke colour
 * (currentColor), no fills, no gradients — it should read like an ink drawing on the green panel.
 */
export function BookIllustration({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 420 260"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.25"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
      aria-hidden
    >
      {/* shelf */}
      <path d="M8 228h404" />
      <path d="M8 236h404" opacity="0.5" />

      {/* spines */}
      <rect x="24" y="92" width="26" height="136" rx="2" />
      <path d="M24 110h26M24 206h26" opacity="0.6" />
      <rect x="54" y="72" width="20" height="156" rx="2" />
      <path d="M58 96h12M58 102h12" opacity="0.6" />
      <rect x="78" y="104" width="30" height="124" rx="2" />
      <path d="M78 122h30M78 212h30" opacity="0.6" />
      <path d="M88 150v36" opacity="0.6" />
      {/* leaning book */}
      <path d="M112 228l26-128 22 4-26 128z" />
      <path d="M131 132l18 3.5" opacity="0.6" />
      <rect x="164" y="84" width="22" height="144" rx="2" />
      <path d="M164 214h22" opacity="0.6" />

      {/* open book */}
      <path d="M214 196c26-12 56-14 92-4v-104c-36-10-66-8-92 4z" />
      <path d="M398 196c-26-12-56-14-92-4v-104c36-10 66-8 92 4z" />
      <path d="M214 196v14c26-12 56-14 92-4 36-10 66-8 92 4v-14" opacity="0.6" />
      {/* text lines, left page */}
      <path d="M228 114c18-6 38-7 62-3M228 128c18-6 38-7 62-3M228 142c18-6 38-7 62-3M228 156c18-6 38-7 46-4" opacity="0.55" />
      {/* text lines, right page */}
      <path d="M322 111c24-4 44-3 62 3M322 125c24-4 44-3 62 3M322 139c24-4 44-3 62 3M322 153c16-3 30-2 40 1" opacity="0.55" />
      {/* ribbon bookmark */}
      <path d="M352 90v44l7-6 7 6V92" />
    </svg>
  );
}
