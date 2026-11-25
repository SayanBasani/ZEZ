/**
 * Converts a board square (1..100) into grid coordinates using serpentine
 * bottom-left to top-left numbering:
 * - Bottom row (rowFromBottom = 0, rowFromTop = 9): 1..10 left-to-right
 * - Second row (rowFromBottom = 1, rowFromTop = 8): 20..11 left-to-right
 * - Top row (rowFromBottom = 9, rowFromTop = 0): 100..91 left-to-right
 */
function squareToCoordinates(square, rows = 10, columns = 10) {
  const boardSize = rows * columns;
  if (!Number.isInteger(square) || square < 1 || square > boardSize) {
    throw new RangeError(`Square must be an integer between 1 and ${boardSize}, received ${square}`);
  }
  const zeroBased = square - 1;
  const rowFromBottom = Math.floor(zeroBased / columns);
  const offset = zeroBased % columns;
  const col = rowFromBottom % 2 === 0 ? offset : columns - 1 - offset;
  const rowFromTop = rows - 1 - rowFromBottom;

  return {
    square,
    row: rowFromTop,
    rowFromTop,
    rowFromBottom,
    col,
  };
}

/**
 * Inverse utility mapping zero-based (rowFromTop, col) back to square number 1..100.
 */
function coordinatesToSquare(rowFromTop, col, rows = 10, columns = 10) {
  if (!Number.isInteger(rowFromTop) || rowFromTop < 0 || rowFromTop >= rows) {
    throw new RangeError(`rowFromTop out of range: ${rowFromTop}`);
  }
  if (!Number.isInteger(col) || col < 0 || col >= columns) {
    throw new RangeError(`col out of range: ${col}`);
  }
  const rowFromBottom = rows - 1 - rowFromTop;
  const offset = rowFromBottom % 2 === 0 ? col : columns - 1 - col;
  return rowFromBottom * columns + offset + 1;
}

/**
 * Normalized board center coordinates in [0..1] for rendering tokens, ladders, and snakes.
 */
function squareCenterNormalized(square, rows = 10, columns = 10) {
  const { rowFromTop, col } = squareToCoordinates(square, rows, columns);
  return {
    square,
    x: (col + 0.5) / columns,
    y: (rowFromTop + 0.5) / rows,
  };
}

module.exports = {
  squareToCoordinates,
  coordinatesToSquare,
  squareCenterNormalized,
};
