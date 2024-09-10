import ExitButton from '@/components/ExitButton';

export default function Game() {
  return (
    <div className='w-full h-screen relative p-1'>
      <div className='absolute right-1 top-1'>
        <ExitButton />
      </div>
      <h2 className='font-omg-body'>Game</h2>
    </div>
  );
}
