import { FormEvent, useState } from 'react'

type Category = {
  label: string
  detail: string
  icon: string
  prompt: string
}

const categories: Category[] = [
  { label: 'Education', detail: 'Scholarships & learning', icon: '✦', prompt: 'I need support for my education.' },
  { label: 'Health', detail: 'Care & wellbeing', icon: '✚', prompt: 'I want to explore health support.' },
  { label: 'Work', detail: 'Jobs & skills', icon: '↗', prompt: 'I am looking for work or skill support.' },
  { label: 'Agriculture', detail: 'Farming & rural life', icon: '⌁', prompt: 'I want to explore support for farmers.' },
  { label: 'Family', detail: 'Housing & care', icon: '◒', prompt: 'I need support for my family.' },
  { label: 'Inclusion', detail: 'Accessible support', icon: '◌', prompt: 'I want to explore inclusive support services.' },
]

function App() {
  const [query, setQuery] = useState('')
  const [assistantMessage, setAssistantMessage] = useState('')

  const beginConversation = (message: string) => {
    const trimmedMessage = message.trim()
    if (!trimmedMessage) return
    setQuery(trimmedMessage)
    setAssistantMessage('Thanks — this prototype would now ask a few simple questions to find potential matches.')
  }

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    beginConversation(query)
  }

  return (
    <main>
      <nav className="nav shell" aria-label="Primary navigation">
        <a className="brand" href="#top" aria-label="Setu home">
          <span className="brand-mark" aria-hidden="true">S</span>
          <span>setu</span>
        </a>
        <span className="prototype-chip">Prototype</span>
        <a className="nav-link" href="#explore">Explore support</a>
      </nav>

      <section className="hero shell" id="top" aria-labelledby="hero-title">
        <div className="hero-copy">
          <p className="eyebrow">Public support, made easier to explore</p>
          <h1 id="hero-title">Start with what matters to you.</h1>
          <p className="lede">Describe your situation in your own words. We’ll help surface public schemes that may be relevant.</p>

          <form className="conversation-card" onSubmit={handleSubmit} aria-label="Start a conversation">
            <label htmlFor="goal">What are you trying to accomplish?</label>
            <div className="composer">
              <input
                id="goal"
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="For example, I need help funding my studies"
              />
              <button type="submit" aria-label="Start conversation">→</button>
            </div>
            <p className="input-hint">Try a sentence, not a form. You can update details anytime.</p>
          </form>

          {assistantMessage && (
            <section className="assistant-reply" aria-live="polite" aria-label="Conversation preview">
              <span className="reply-orb" aria-hidden="true">✦</span>
              <p>{assistantMessage}</p>
            </section>
          )}
        </div>

        <div className="hero-art" aria-hidden="true">
          <div className="sun"></div>
          <div className="arch arch-back"></div>
          <div className="arch arch-front"></div>
          <div className="path path-one"></div>
          <div className="path path-two"></div>
          <span className="art-label label-top">a clearer path</span>
          <span className="art-label label-bottom">one conversation</span>
        </div>
      </section>

      <section className="explore shell" id="explore" aria-labelledby="explore-title">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Or begin with a topic</p>
            <h2 id="explore-title">Explore support by category</h2>
          </div>
          <p>Choose a starting point and we’ll shape the conversation around it.</p>
        </div>

        <div className="category-grid">
          {categories.map((category) => (
            <button className="category-card" key={category.label} type="button" onClick={() => beginConversation(category.prompt)}>
              <span className="category-icon" aria-hidden="true">{category.icon}</span>
              <span className="category-content"><strong>{category.label}</strong><small>{category.detail}</small></span>
              <span className="category-arrow" aria-hidden="true">→</span>
            </button>
          ))}
        </div>
      </section>

      <section className="how-it-works shell" aria-labelledby="how-title">
        <p className="eyebrow">A thoughtful starting point</p>
        <h2 id="how-title">A simple way to find your way</h2>
        <div className="steps">
          <article><span>01</span><h3>Tell us your goal</h3><p>Start with the outcome you have in mind, in everyday language.</p></article>
          <article><span>02</span><h3>Answer only what helps</h3><p>The conversation can clarify the details that make a difference.</p></article>
          <article><span>03</span><h3>Understand your options</h3><p>Review potential matches, requirements and next steps in one place.</p></article>
        </div>
      </section>

      <footer className="disclaimer">
        <div className="shell disclaimer-content">
          <span className="notice-icon" aria-hidden="true">i</span>
          <p><strong>Prototype using curated mock data.</strong> This independent concept is not an official UMANG product or government service. It does not determine final eligibility; the relevant authority does.</p>
        </div>
      </footer>
    </main>
  )
}

export default App
