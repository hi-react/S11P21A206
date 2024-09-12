/** @type {import('tailwindcss').Config} */

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  important: true,
  theme: {
    fontSize: {
      'omg-xl': '1.4rem', // 이벤트 발생 알림
      'omg-lg': '1.2rem', // 행위 선택 버튼
      'omg-md': '1rem', // normal
      'omg-sm': '0.7rem', // 토큰 등 정보 제공
      'omg-xs': '0.5rem', // 채팅
    },
    // colors: {},
    // borderRadius: {},
    // dropShadow: {},
    extend: {
      fontFamily: {
        'omg-title': 'OKDDUNG',
        'omg-body': 'Katuri',
      },
    },
  },
  plugins: [],
};
