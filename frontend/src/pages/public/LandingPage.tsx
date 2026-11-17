import { Link } from 'react-router-dom';
import type { ReactNode } from 'react';
import SearchIcon from '@mui/icons-material/Search';
import InfoOutlined from '@mui/icons-material/InfoOutlined';
import MenuBook from '@mui/icons-material/MenuBook';
import MailOutline from '@mui/icons-material/MailOutline';
import ArrowForward from '@mui/icons-material/ArrowForward';
import Summarize from '@mui/icons-material/Summarize';
import FileDownload from '@mui/icons-material/FileDownload';
import Place from '@mui/icons-material/Place';
import PersonAdd from '@mui/icons-material/PersonAdd';

/**
 * index.zul + landing.zul (route "/"). Tailwind-styled (preflight off, see tailwind.config.js).
 * All links are React Router routes: /search, /register, /about, /manual, /contact.
 */

/** Partners exactly as named in the SIBOL project text. */
const PARTNERS = [
  { abbr: 'USAID', name: 'United States Agency for International Development', role: 'Funder' },
  { abbr: 'RTI', name: 'RTI International', role: 'Implementer' },
  { abbr: 'CCI', name: 'Center for Conservation Innovations', role: 'Consortium partner' },
  { abbr: 'FFP', name: 'Forest Foundation Philippines', role: 'Consortium partner' },
  { abbr: 'REECS', name: 'Resources, Environment, and Economics Center for Studies, Inc.', role: 'Consortium partner' },
  { abbr: 'ZSL', name: 'Zoological Society of London', role: 'Consortium partner' },
];

/** What the "Database" option offers, per the original landing text. */
const FEATURES = [
  { icon: <SearchIcon />, title: 'Search studies', text: 'Filter valuation studies by ecosystem, service, method, country or keyword.' },
  { icon: <Summarize />, title: 'View study summaries', text: 'See sites, study focus, methods and every recorded value of a study.' },
  { icon: <FileDownload />, title: 'Export to Excel', text: 'Download your search results, one row per value, for your own analysis.' },
  { icon: <Place />, title: 'Map the results', text: 'Visualize study sites of your search results on Google Maps.' },
];

const QUICK_LINKS = [
  { to: '/search', label: 'Database', text: 'Search studies and values', icon: <SearchIcon fontSize="small" /> },
  { to: '/about', label: 'About SIBOL-EVES', text: 'Purpose and types of information', icon: <InfoOutlined fontSize="small" /> },
  { to: '/manual', label: 'User Manual', text: 'Step-by-step guide (web and PDF)', icon: <MenuBook fontSize="small" /> },
  { to: '/contact', label: 'Contact Us', text: 'Questions, suggestions, bug reports', icon: <MailOutline fontSize="small" /> },
];

const focusRing = 'focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2';

export function LandingPage() {
  return (
    <div className="font-sans text-slate-800">
      {/* ------------------------------------------------------------------ hero */}
      <section className="relative overflow-hidden border-b border-solid border-emerald-100 bg-gradient-to-br from-emerald-50 via-green-50 to-teal-50 text-[#0f3822]">
        <div aria-hidden className="pointer-events-none absolute -left-24 -top-24 h-80 w-80 rounded-full bg-emerald-200/40 blur-3xl" />
        <div aria-hidden className="pointer-events-none absolute -bottom-32 right-0 h-96 w-96 rounded-full bg-teal-200/30 blur-3xl" />
        <div className="relative mx-auto grid max-w-7xl items-center gap-10 px-4 py-12 sm:px-6 sm:py-16 lg:grid-cols-12 lg:px-8 lg:py-20">
          {/* SIBOL artwork: left on desktop, below the text on phones/tablets */}
          <div className="order-2 lg:order-1 lg:col-span-5">
            <img
              src="/img/sibol-artwork.webp"
              srcSet="/img/sibol-artwork-600.webp 600w, /img/sibol-artwork.webp 1200w"
              sizes="(min-width: 1024px) 40vw, 100vw"
              width={1200}
              height={456}
              loading="eager"
              fetchPriority="high"
              alt="SIBOL artwork: giant clam, myna, pangolin, sailfish and tarsier, with logos of DENR, BFAR, USAID, RTI International, CCI, Forest Foundation Philippines, REECS and ZSL"
              className="block h-auto w-full rounded-2xl shadow-xl ring-1 ring-emerald-900/10"
            />
          </div>
          {/* right-aligned banner content */}
          <div className="order-1 flex flex-col items-end text-right lg:order-2 lg:col-span-7">
            <p className="m-0 inline-flex items-center gap-2 self-end rounded-full border border-solid border-emerald-200 bg-white/70 px-3 py-1 text-xs font-semibold uppercase tracking-widest text-emerald-900 backdrop-blur-md">
              Ecosystem services valuation database
            </p>
            <h1 className="mb-0 mt-5 text-4xl font-extrabold leading-tight tracking-tight text-[#0f3822] sm:text-5xl lg:text-6xl">
              Economic values of nature in the Philippines and Southeast Asia
            </h1>
            <p className="mb-0 ml-auto mt-5 max-w-2xl text-lg leading-relaxed text-slate-800">
              Search studies, compare ecosystem service values, export results to Excel and see study sites on a map.
            </p>
            <div className="mt-8 flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:justify-end">
              <Link
                to="/search"
                className={`inline-flex items-center justify-center gap-2 rounded-xl bg-emerald-800 px-6 py-3 text-base font-bold text-white no-underline shadow-lg shadow-emerald-900/20 transition hover:-translate-y-0.5 hover:bg-emerald-900 ${focusRing} focus-visible:ring-emerald-700 focus-visible:ring-offset-emerald-50`}
              >
                <SearchIcon fontSize="small" /> Search the database
              </Link>
              <Link
                to="/register"
                className={`inline-flex items-center justify-center gap-2 rounded-xl border border-solid border-emerald-700 bg-white/70 px-6 py-3 text-base font-semibold text-[#0f3822] no-underline backdrop-blur-md transition hover:bg-white ${focusRing} focus-visible:ring-emerald-700 focus-visible:ring-offset-emerald-50`}
              >
                <PersonAdd fontSize="small" /> Create a free account
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* ------------------------------------------------------------------ features */}
      <section className="relative z-10 mx-auto -mt-8 max-w-7xl px-4 sm:px-6 lg:px-8">
        <ul className="m-0 grid list-none gap-4 p-0 sm:grid-cols-2 lg:grid-cols-4">
          {FEATURES.map((f) => (
            <li key={f.title} className="rounded-2xl border border-solid border-slate-200 bg-white p-5 shadow-sm">
              <span className="inline-flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-50 text-emerald-700">{f.icon}</span>
              <h2 className="mb-1 mt-3 text-base font-bold text-slate-900">{f.title}</h2>
              <p className="m-0 text-sm leading-relaxed text-slate-600">{f.text}</p>
            </li>
          ))}
        </ul>
      </section>

      {/* ------------------------------------------------------------------ main + quick navigation */}
      <section className="mx-auto grid max-w-7xl gap-6 px-4 py-12 sm:px-6 lg:grid-cols-3 lg:px-8">
        <div className="flex flex-col gap-6 lg:col-span-2">
          <Card title="About the SIBOL project">
            <p className="m-0 leading-relaxed text-slate-700">
              SIBOL is a five-year (2020-2024) project focused on natural resources manangement and biodiversity conservation. It is funded by the United
              States Agency for International Development (USAID) and implemented by the RTI International, along with consortium partners including the
              Center for Conservation Innovations (CCI), the Forest Foundation Philippines (FFP), the Resources, Environment, and Economics Center for
              Studies, Inc. (REECS); and the Zoological Society of London (ZSL).
            </p>
            <ul className="m-0 mt-5 grid list-none gap-2 p-0 sm:grid-cols-2">
              {PARTNERS.map((p) => (
                <li key={p.abbr} className="flex items-start gap-3 rounded-xl border border-solid border-slate-200 bg-slate-50 px-3 py-2">
                  <span className="mt-0.5 inline-flex min-w-[3.25rem] justify-center rounded-lg bg-emerald-800 px-2 py-0.5 text-xs font-bold text-white">{p.abbr}</span>
                  <span className="text-sm leading-snug">
                    <span className="block font-semibold text-slate-800">{p.name}</span>
                    <span className="text-slate-500">{p.role}</span>
                  </span>
                </li>
              ))}
            </ul>
          </Card>

          <Card title="Getting started">
            <p className="m-0 leading-relaxed text-slate-700">
              The SIBOL-EVES database is designed to support policy analysts and researchers in the valuation of ecosystem services.
            </p>
            <ol className="m-0 mt-5 grid list-none gap-3 p-0">
              <Step n={1} title="Create a free account">
                To access the database, you can easily create a free account by <InlineLink to="/register">registering</InlineLink>.
              </Step>
              <Step n={2} title="Search the database">
                The "<InlineLink to="/search">Database</InlineLink>" option in the menu panel enables you to search for studies, view study summaries,
                export search results to Excel format, and visualize search results on Google Maps.
              </Step>
              <Step n={3} title="Learn more">
                For further details and guidance on using the database, refer to the "<InlineLink to="/about">About SIBOL-EVES</InlineLink>" section and
                consult the <InlineLink to="/manual">User Manual</InlineLink>.
              </Step>
              <Step n={4} title="Ask or suggest">
                If you have any questions or suggestions, please use the "<InlineLink to="/contact">Contact Us</InlineLink>" option. We value and appreciate
                your feedback and contributions.
              </Step>
            </ol>
          </Card>
        </div>

        <aside aria-labelledby="quick-nav-title" className="lg:sticky lg:top-24 lg:self-start">
          <nav className="rounded-2xl border border-solid border-slate-200 bg-white/80 p-5 shadow-sm backdrop-blur-md">
            <h2 id="quick-nav-title" className="m-0 text-sm font-bold uppercase tracking-widest text-slate-500">Quick Navigation</h2>
            <ul className="m-0 mt-4 grid list-none gap-2 p-0">
              {QUICK_LINKS.map((l) => (
                <li key={l.to}>
                  <Link
                    to={l.to}
                    className={`group flex items-center gap-3 rounded-xl border border-solid border-transparent px-3 py-3 text-slate-800 no-underline transition hover:border-emerald-200 hover:bg-emerald-50 ${focusRing} focus-visible:ring-emerald-600`}
                  >
                    <span className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-emerald-100 text-emerald-800">{l.icon}</span>
                    <span className="min-w-0 flex-1">
                      <span className="block font-semibold">{l.label}</span>
                      <span className="block text-sm text-slate-500">{l.text}</span>
                    </span>
                    <ArrowForward fontSize="small" className="text-slate-400 transition group-hover:translate-x-0.5 group-hover:text-emerald-700" />
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
        </aside>
      </section>

      {/* ------------------------------------------------------------------ partner logos */}
      <section className="mx-auto max-w-7xl px-4 pb-14 sm:px-6 lg:px-8">
        <div className="rounded-2xl border border-solid border-slate-200 bg-white p-4 shadow-sm sm:p-6">
          <img src="/img/partner-logos.png" alt="Logos of USAID, RTI International, CCI, FFP, REECS and ZSL" className="block w-full" />
        </div>
      </section>
    </div>
  );
}

function Card({ title, children }: { title: string; children: ReactNode }) {
  return (
    <article className="rounded-2xl border border-solid border-slate-200 bg-white p-6 shadow-sm sm:p-8">
      <h2 className="m-0 mb-4 font-serif text-2xl font-semibold text-slate-900">{title}</h2>
      {children}
    </article>
  );
}

function Step({ n, title, children }: { n: number; title: string; children: ReactNode }) {
  return (
    <li className="flex gap-4">
      <span className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-emerald-700 text-sm font-bold text-white">{n}</span>
      <div>
        <h3 className="m-0 text-base font-bold text-slate-900">{title}</h3>
        <p className="m-0 mt-1 leading-relaxed text-slate-600">{children}</p>
      </div>
    </li>
  );
}

function InlineLink({ to, children }: { to: string; children: ReactNode }) {
  return (
    <Link to={to} className="font-semibold text-emerald-700 underline decoration-emerald-300 underline-offset-2 hover:text-emerald-900">
      {children}
    </Link>
  );
}
