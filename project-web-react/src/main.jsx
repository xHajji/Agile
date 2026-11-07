import { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';

function App() {
  const [message, setMessage] = useState('Sprawdzam połączenie z API i bazą…');

  useEffect(() => {
    const controller = new AbortController();
    const signal = AbortSignal.any([controller.signal, AbortSignal.timeout(5000)]);
    const unavailable = 'Brak połączenia z API lub bazą. Uruchom oba serwisy i odśwież stronę.';

    async function checkConnection() {
      try {
        const response = await fetch('/actuator/health', { signal });
        const health = response.ok ? await response.json() : null;
        setMessage(health?.status === 'UP' ? 'Połączenie z API i bazą działa.' : unavailable);
      } catch {
        if (!controller.signal.aborted) {
          setMessage(unavailable);
        }
      }
    }

    checkConnection();
    return () => controller.abort();
  }, []);

  return (
    <main>
      <h1>Agile · React</h1>
      <p>React tworzy tę stronę w przeglądarce. Vite przekazuje żądanie do wspólnego API.</p>
      <p role="status">{message}</p>
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
