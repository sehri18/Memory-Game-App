package com.example.memoryapp

import android.animation.ArgbEvaluator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoryapp.models.BoardSize
import com.example.memoryapp.models.MemoryGame
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
    }
    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView

    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdaptor
    private var boardSize: BoardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        setupBoard()

    }



    //MENU
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_refresh -> {
                if (memoryGame.numPairsFound > 0 && !memoryGame.haveWonGame()) {
                    showAlertDialog("Quit your game?", null, View.OnClickListener {
                        setupBoard()
                    })
                } else {
                    setupBoard()
                }
                return true
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when(boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rb_easy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rb_medium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rb_hard)
        }

        showAlertDialog("Choose new size",boardSizeView, View.OnClickListener {
            //Set a new value for the board size
            boardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rb_easy ->  BoardSize.EASY
                R.id.rb_medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        })
    }

    private fun showAlertDialog(title : String, view: View?, positiveClickListener : View.OnClickListener) {
        AlertDialog.Builder(this)
                .setTitle(title)
                .setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK") { _, _ ->
                        positiveClickListener.onClick(null)
                }.show()

    }


    private fun setupBoard() {
        when(boardSize)
        {
            BoardSize.EASY -> {
                tvNumMoves.text = "Easy: 4 x 2"
                tvNumPairs.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "Medium: 6 x 3"
                tvNumPairs.text = "Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text = "Hard: 6 x 4"
                tvNumPairs.text = "Pairs: 0 / 12"
            }
        }


        tvNumPairs.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))

        memoryGame = MemoryGame(boardSize)

        // Defining the Adaptor that will provide a binding for the data set to the Recyclerview.
        // A new class: MemoryBoardAdaptor
        adapter = MemoryBoardAdaptor(this, boardSize, memoryGame.cards, object: MemoryBoardAdaptor.CardClickListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        })
        rvBoard.adapter = adapter

        //This property improves the efficiency of our app.
        rvBoard.setHasFixedSize(true)

        // Defining the Grid layout for the cards to be displayed.
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }


    private fun updateGameWithFlip(position: Int) {
        //Error handling
        if(memoryGame.haveWonGame()) {
            // Alert the user that its an invalid move
            Snackbar.make(clRoot, "You already won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if(memoryGame.isCardFaceUp(position)){
            // Alert the user that its an invalid move
            Snackbar.make(clRoot, "Invalid move", Snackbar.LENGTH_SHORT).show()
            return
        }
        //Card is flipped over
        if(memoryGame.flipCard(position)){
            Log.i(TAG, "You found a match! Number of pairs found ${memoryGame.numPairsFound}")
            val color = ArgbEvaluator().evaluate(
                   memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                    ContextCompat.getColor(this, R.color.color_progress_none),
                    ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int

            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if(memoryGame.haveWonGame()){
                Snackbar.make(clRoot, "You won the game!", Snackbar.LENGTH_LONG).show()
            }
        }
        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"

        adapter.notifyDataSetChanged()
    }
}