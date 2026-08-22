import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import App from './App'

describe('App', () => {
  it('renders the primary conversational entry point and prototype disclaimer', () => {
    render(<App />)

    expect(screen.getByLabelText('What are you trying to accomplish?')).toBeInTheDocument()
    expect(screen.getByText(/Prototype using curated mock data/i)).toBeInTheDocument()
    expect(screen.getByText(/not an official UMANG product/i)).toBeInTheDocument()
  })

  it('starts the mocked conversation when the citizen submits a goal', async () => {
    const user = userEvent.setup()
    render(<App />)

    await user.type(screen.getByLabelText('What are you trying to accomplish?'), 'I need help with studies')
    await user.click(screen.getByRole('button', { name: 'Start conversation' }))

    expect(screen.getByLabelText('Conversation preview')).toHaveTextContent(/would now ask a few simple questions/i)
  })

  it('uses a category shortcut to start a conversation', async () => {
    const user = userEvent.setup()
    render(<App />)

    await user.click(screen.getByRole('button', { name: /Education/i }))

    expect(screen.getByLabelText('What are you trying to accomplish?')).toHaveValue('I need support for my education.')
    expect(screen.getByLabelText('Conversation preview')).toBeInTheDocument()
  })
})
