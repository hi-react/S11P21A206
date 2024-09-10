import { Suspense, lazy } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';

import './index.css';

const App = lazy(() => import('./App'));

createRoot(document.getElementById('root')!).render(
  <BrowserRouter>
    <Suspense fallback={<div>앱 불러오는 중...</div>}>
      <App />
    </Suspense>
  </BrowserRouter>,
);
